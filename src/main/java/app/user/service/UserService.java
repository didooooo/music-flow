package app.user.service;

import app.address.model.Address;
import app.address.service.AddressService;
import app.notification.dto.Notification;
import app.notification.service.NotificationService;
import app.order.model.Order;
import app.record.model.Record;
import app.review.model.Review;
import app.security.AuthUser;
import app.shopping_cart.model.ShoppingCartInfo;
import app.shopping_cart.service.ShoppingCartInfoService;
import app.shopping_cart.service.ShoppingCartService;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.EditAccountRequest;
import app.web.dto.RegisterRequest;
import app.notification.dto.SendEmailRequest;
import app.web.dto.UserFilterRequest;
import jakarta.transaction.Transactional;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final StatisticService statisticService;
    private final ConversionService conversionService;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final ShoppingCartService shoppingCartService;
    private final ShoppingCartInfoService shoppingCartInfoService;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, StatisticService statisticService, ConversionService conversionService, PasswordEncoder passwordEncoder, AddressService addressService, ShoppingCartService shoppingCartService, ShoppingCartInfoService shoppingCartInfoService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.statisticService = statisticService;
        this.conversionService = conversionService;
        this.passwordEncoder = passwordEncoder;
        this.addressService = addressService;
        this.shoppingCartService = shoppingCartService;
        this.shoppingCartInfoService = shoppingCartInfoService;
        this.notificationService = notificationService;
    }

    @Transactional
    public User registerUser(RegisterRequest input) {
        Optional<User> userWithUsername = userRepository.findByUsername(input.getUsername());
        if (userWithUsername.isPresent()) {
            throw new RuntimeException("Username is already in use");
        }
        Optional<User> userWithEmail = userRepository.findByEmail(input.getEmail());
        if (userWithEmail.isPresent()) {
            throw new RuntimeException("Email is already in use");
        }
        Address address = addressService.save(input.getStreet(), input.getCity(), input.getState(), input.getZip());
        User user = conversionService.convert(input, User.class)
                .toBuilder()
                .password(passwordEncoder.encode(input.getPassword()))
                .address(address)
                .build();
        User saved = userRepository.save(user);
        Statistics statisticsForToday = statisticService.getStatisticsForToday();
        statisticsForToday.setActiveUsers(statisticsForToday.getActiveUsers() + 1);
        statisticsForToday.setTotalCustomers(statisticsForToday.getTotalCustomers() + 1);
        statisticService.save(statisticsForToday);
        return saved;
    }

    public User getById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("There is no such user with [%s] ".formatted(userId)));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("There is no such user with [%s] ".formatted(username)));
        return new AuthUser(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void addRecordToWishlist(UUID userId, Record record) {
        User user = getById(userId);
        for (Record r : user.getWishlist()) {
            if (r.getId().equals(record.getId())) {
                return;
            }
        }
        user.getWishlist().add(record);
        userRepository.save(user);
    }

    @Transactional
    public void addRecordToCart(Record record, UUID userId) {
        User user = getById(userId);
        if (user.getShoppingCart() != null) {
            for (ShoppingCartInfo product : user.getShoppingCart().getShoppingCartInfos()) {
                if (product.getRecord().getId().equals(record.getId())) {
                    product.setQuantity(product.getQuantity() + 1);
                    user.getShoppingCart().setTotalPrice(user.getShoppingCart().getTotalPrice().add(record.getPrice()));
                    user.getShoppingCart().setTotalQuantity(user.getShoppingCart().getTotalQuantity() + 1);
                    userRepository.save(user);
                    return;
                }
            }
        } else {
            user.setShoppingCart(shoppingCartService.createShoppingCartForUser(user));
        }

        user.getShoppingCart().getShoppingCartInfos().add(shoppingCartInfoService.addNewProductInShoppingCart(record, user.getShoppingCart()));
        userRepository.save(user);
    }

    public void addReviewToUserProfile(User user, Review saved) {
        user.getReviews().add(saved);
        userRepository.save(user);
    }

    public List<ShoppingCartInfo> getShoppingCartRecords(UUID userId) {
        User user = getById(userId);
        return user.getShoppingCart().getShoppingCartInfos();
    }

    public void removeFromWishlist(User user, Record record) {
        for (Record r : user.getWishlist()) {
            if (r.getId().equals(record.getId())) {
                user.getWishlist().remove(r);
                userRepository.save(user);
                return;
            }
        }
    }

    public void addOrderToUserOrders(Order saved, User fromDB) {
        fromDB.getOrders().add(saved);
        userRepository.save(fromDB);
    }

    public void clearShoppingCart(User fromDB) {
        fromDB.getShoppingCart().getShoppingCartInfos().clear();
        fromDB.getShoppingCart().setTotalQuantity(0);
        fromDB.getShoppingCart().setTotalPrice(BigDecimal.ZERO);
        userRepository.save(fromDB);
    }

    public Page<User> getEightUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void switchUserRole(User user, User loggedInUser) {
        if (loggedInUser.getId().equals(user.getId())) {
            return;
        }
        if (user.getRole().equals(Role.USER)) {
            user.setRole(Role.ADMIN);
            userRepository.save(user);
            return;
        }
        user.setRole(Role.USER);
        userRepository.save(user);
    }

    @Transactional
    public void switchUserStatus(User user, User loggedInUser) {
        if (loggedInUser.getId().equals(user.getId())) {
            return;
        }
        user.setActive(!user.isActive());
        userRepository.save(user);
        Statistics statisticsForToday = statisticService.getStatisticsForToday();
        if (user.isActive()) {
            statisticsForToday.setActiveUsers(statisticsForToday.getActiveUsers() + 1);
            statisticsForToday.setInactiveUsers(statisticsForToday.getInactiveUsers() - 1);
            statisticService.save(statisticsForToday);
            return;
        }
        statisticsForToday.setInactiveUsers(statisticsForToday.getInactiveUsers() + 1);
        statisticsForToday.setActiveUsers(statisticsForToday.getActiveUsers() - 1);
        statisticService.save(statisticsForToday);
    }

    public Page<User> getEightUsers(PageRequest of, UserFilterRequest userFilterRequest) {
        Specification<User> spec = Specification.where(null);
        spec = criteriaBuilder(spec, userFilterRequest);
        String sort = userFilterRequest.getSearchedSort();
        if (sort == null) {
            return userRepository.findAll(spec, of);
        } else if (sort.equals("date")) {
            return userRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.ASC, "createdAt")));
        } else if (sort.equals("nameAsc")) {
            return userRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.ASC, "username")));
        } else if (sort.equals("nameDesc")) {
            return userRepository.findAll(spec, PageRequest.of(of.getPageNumber(), of.getPageSize(), Sort.by(Sort.Direction.DESC, "username")));
        }
        return userRepository.findAll(spec, of);
    }

    private Specification<User> criteriaBuilder(Specification<User> spec, UserFilterRequest userFilterRequest) {
        if (!userFilterRequest.getSearchedName().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> {
                return criteriaBuilder.like(root.get("username"), "%" + userFilterRequest.getSearchedName() + "%");
            });
        }
        if (!userFilterRequest.getSearchedStatus().isEmpty()) {
            boolean active;
            if (userFilterRequest.getSearchedStatus().equalsIgnoreCase("active")) {
                active = true;
            } else {
                active = false;
            }
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("active")).value(active));
        }
        return spec;
    }

    public User editInfo(User user, EditAccountRequest editAccountRequest) {
        user.setEmail(editAccountRequest.getEmail());
        user.setFirstName(editAccountRequest.getFirstName());
        user.setLastName(editAccountRequest.getLastName());
        if (!(editAccountRequest.getPassword() == null || editAccountRequest.getPassword().isBlank())) {
            user.setPassword(passwordEncoder.encode(editAccountRequest.getPassword()));
        }
        user.setPhone(editAccountRequest.getPhone());
        if (!(editAccountRequest.getUrlPhoto() == null || editAccountRequest.getUrlPhoto().isBlank())) {
            user.setProfilePicture(editAccountRequest.getUrlPhoto());
        }
        Address address = addressService.save(editAccountRequest.getStreet(), editAccountRequest.getCity(), editAccountRequest.getState(), editAccountRequest.getZip());
        user.setAddress(address);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(User user) {
        Statistics statisticsForToday = statisticService.getStatisticsForToday();
        statisticsForToday.setActiveUsers(statisticsForToday.getActiveUsers() - 1);
        statisticsForToday.setInactiveUsers(statisticsForToday.getInactiveUsers() + 1);
        statisticService.save(statisticsForToday);
        user.setActive(false);
        userRepository.save(user);
    }

    public User getTopCustomer() {
        List<User> users = userRepository.findAll();
        User topCustomer = null;
        BigDecimal maxSpent = BigDecimal.ZERO;
        int maxOrders = 0;
        for (User user : users) {
            BigDecimal totalSpent = BigDecimal.ZERO;
            int orderCount = user.getOrders().size();
            for (Order order : user.getOrders()) {
               totalSpent=totalSpent.add(order.getTotalPrice());
            }
            if (totalSpent.compareTo(maxSpent)>0 || (totalSpent.compareTo(maxSpent)==0 && orderCount > maxOrders)) {
                maxSpent = totalSpent;
                maxOrders = orderCount;
                topCustomer = user;
            }
        }
        return topCustomer;
    }

    public BigDecimal getTotalSpentMoneyByGivenUser(User user) {
        BigDecimal totalSpent = BigDecimal.ZERO;
        for (Order order : user.getOrders()) {
            totalSpent=totalSpent.add(order.getTotalPrice());
        }
        return totalSpent;
    }

    public User getUserByEmail(String receiver) {
        return userRepository.findByEmail(receiver).orElseThrow(()->new RuntimeException("User not found"));
    }
}
