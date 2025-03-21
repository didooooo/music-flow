package app.user.service;

import app.address.model.Address;
import app.address.service.AddressService;
import app.order.model.Order;
import app.record.model.Record;
import app.review.model.Review;
import app.security.AuthUser;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.model.ShoppingCartInfo;
import app.shopping_cart.service.ShoppingCartInfoService;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.web.dto.RegisterRequest;
import jakarta.transaction.Transactional;
import org.springframework.core.convert.ConversionService;
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
    private final ConversionService conversionService;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;
    private final ShoppingCartService shoppingCartService;
    private final ShoppingCartInfoService shoppingCartInfoService;

    public UserService(UserRepository userRepository, ConversionService conversionService, PasswordEncoder passwordEncoder, AddressService addressService, ShoppingCartService shoppingCartService, ShoppingCartInfoService shoppingCartInfoService) {
        this.userRepository = userRepository;
        this.conversionService = conversionService;
        this.passwordEncoder = passwordEncoder;
        this.addressService = addressService;
        this.shoppingCartService = shoppingCartService;
        this.shoppingCartInfoService = shoppingCartInfoService;
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
        return userRepository.save(user);
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
                    user.getShoppingCart().setTotalQuantity(user.getShoppingCart().getTotalQuantity()+1);
                    userRepository.save(user);
                    return;
                }
            }
        }else{
            user.setShoppingCart(shoppingCartService.createShoppingCartForUser(user));
        }

        user.getShoppingCart().getShoppingCartInfos().add(shoppingCartInfoService.addNewProductInShoppingCart(record,user.getShoppingCart()));
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
}
