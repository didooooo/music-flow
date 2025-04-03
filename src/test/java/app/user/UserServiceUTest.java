package app.user;

import app.address.model.Address;
import app.address.service.AddressService;
import app.exception.EmailAlreadyExistException;
import app.exception.UsernameAlreadyExistException;
import app.notification.service.NotificationService;
import app.order.model.Order;
import app.record.model.Record;
import app.review.model.Review;
import app.security.AuthUser;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.model.ShoppingCartInfo;
import app.shopping_cart.service.ShoppingCartInfoService;
import app.shopping_cart.service.ShoppingCartService;
import app.statistics.model.Statistics;
import app.statistics.service.StatisticService;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.EditAccountRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private StatisticService statisticService;
    @Mock
    private ConversionService conversionService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AddressService addressService;
    @Mock
    private ShoppingCartService shoppingCartService;
    @Mock
    private ShoppingCartInfoService shoppingCartInfoService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private UserService userService;

    @Test
    void givenRegisterRequest_whenRegister_thenRegisterUser() {
        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("username")
                .password("password")
                .email("email@gmail.com")
                .phone("phone")
                .city("city")
                .state("state")
                .zip("9000")
                .street("street")
                .profilePicture("www.image.com")
                .build();
        UUID addressId = UUID.randomUUID();
        Address address = new Address(addressId,
                registerRequest.getStreet(),
                registerRequest.getCity(),
                registerRequest.getState(),
                registerRequest.getZip());
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("username")
                .password("$2b$12$da71Fv8fxpvl0FcRId14NO4WnPKmEGPVE/xMhKPa4/5LNIVp7GIR2")
                .address(address)
                .build();
        Statistics statistics = new Statistics();
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(addressService.save(any(), any(), any(), any())).thenReturn(address);
        when(conversionService.convert(registerRequest, User.class)).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(statisticService.getStatisticsForToday()).thenReturn(statistics);
        when(passwordEncoder.encode("password")).thenReturn("$2b$12$da71Fv8fxpvl0FcRId14NO4WnPKmEGPVE/xMhKPa4/5LNIVp7GIR2");
        //When
        User registeredUser = userService.registerUser(registerRequest);
        //Then
        assertNotNull(registeredUser);
        assertEquals("username", registeredUser.getUsername());
        assertEquals("$2b$12$da71Fv8fxpvl0FcRId14NO4WnPKmEGPVE/xMhKPa4/5LNIVp7GIR2", registeredUser.getPassword());
        assertEquals(addressId, registeredUser.getAddress().getId());
        verify(conversionService, times(1)).convert(registerRequest, User.class);
        verify(passwordEncoder, times(1)).encode(any());
        verify(statisticService, times(1)).getStatisticsForToday();
        verify(addressService, times(1)).save(any(), any(), any(), any());
        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, times(1)).findByUsername(any());
        verify(statisticService, times(1)).save(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void givenExistingUsername_whenRegister_thenThrowUsernameAlreadyExistException() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("existingUser")
                .password("password")
                .email("email@gmail.com")
                .build();

        User existingUser = new User();
        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(UsernameAlreadyExistException.class, () -> {
            userService.registerUser(registerRequest);
        });

        verify(userRepository, times(1)).findByUsername(registerRequest.getUsername());
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowEmailAlreadyExistException() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("newUser")
                .password("password")
                .email("existing@email.com")
                .build();

        User existingUser = new User();
        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(EmailAlreadyExistException.class, () -> {
            userService.registerUser(registerRequest);
        });

        verify(userRepository, times(1)).findByUsername(registerRequest.getUsername());
        verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any());
    }

    //     public User getById(UUID userId) {
//        return userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("There is no such user with [%s] ".formatted(userId)));
//    }
    @Test
    void givenExistingUserId_whenGetById_thenReturnUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("testUser")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User foundUser = userService.getById(userId);

        // Then
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals("testUser", foundUser.getUsername());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void givenNonExistingUserId_whenGetById_thenThrowUsernameNotFoundException() {
        // Given
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenThrow(UsernameNotFoundException.class);

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.getById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void givenExistingUser_whenLoadUserByUsername_thenReturnCorrectAuthenticationMetadata() {

        // Given
        String username = "username";
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .password("password")
                .role(Role.ADMIN)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails authenticationMetadata = userService.loadUserByUsername(username);

        // Then
        assertInstanceOf(AuthUser.class, authenticationMetadata);
        AuthUser result = (AuthUser) authenticationMetadata;
        assertEquals(user.getId(), result.getUserId());
        assertEquals(username, result.getUsername());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.isActive(), result.isActive());
        assertEquals(user.getRole(), result.getRole());
        assertThat(result.getAuthorities()).hasSize(1);
        assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void givenMissingUserFromDatabase_whenLoadUserByUsername_thenExceptionIsThrown() {

        // Given
        String username = "username";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(username));
    }

    @Test
    void givenUsersInDatabase_whenGetAllUsers_thenReturnUserList() {
        // Given
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .password("encodedPassword1")
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .username("user2")
                .password("encodedPassword2")
                .build();

        List<User> users = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void givenNoUsersInDatabase_whenGetAllUsers_thenReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void givenRecordAlreadyInWishlist_whenAddRecordToWishlist_thenDoNothing() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        Record record = Record.builder().id(recordId).build();
        User user = User.builder()
                .id(userId)
                .wishlist(new ArrayList<>(List.of(record)))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.addRecordToWishlist(userId, record);

        // Then
        assertEquals(1, user.getWishlist().size());
        verify(userRepository, never()).save(any());
    }

    @Test
    void givenRecordNotInWishlist_whenAddRecordToWishlist_thenAddRecord() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        Record record = Record.builder().id(recordId).build();
        User user = User.builder()
                .id(userId)
                .wishlist(new ArrayList<>())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.addRecordToWishlist(userId, record);

        // Then
        assertEquals(1, user.getWishlist().size());
        assertEquals(recordId, user.getWishlist().getFirst().getId());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenNotExistingUserId_whenAddRecordToWishlist_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        Record record = Record.builder().id(recordId).build();
        User user = User.builder()
                .id(userId)
                .wishlist(new ArrayList<>())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When and Then
        assertThrows(UsernameNotFoundException.class, () -> userService.addRecordToWishlist(userId, record));
        verify(userRepository, never()).save(user);
    }

    @Test
    void givenReview_whenAddReviewToUserProfile_thenAddReviewAndSaveUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .reviews(new ArrayList<>())
                .build();

        Review review = Review.builder()
                .id(UUID.randomUUID())
                .description("Great product!")
                .build();

        // When
        userService.addReviewToUserProfile(user, review);

        // Then
        assertEquals(1, user.getReviews().size());
        assertEquals("Great product!", user.getReviews().getFirst().getDescription());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingUserWithShoppingCart_whenGetShoppingCartRecords_thenReturnShoppingCartInfoList() {
        // Given
        UUID userId = UUID.randomUUID();
        ShoppingCartInfo cartInfo1 = new ShoppingCartInfo(UUID.randomUUID(), Record.builder().title("Product 1").build(), null, 100);
        ShoppingCartInfo cartInfo2 = new ShoppingCartInfo(UUID.randomUUID(), Record.builder().title("Product 2").build(), null, 50);

        ShoppingCart shoppingCart = ShoppingCart.builder().shoppingCartInfos(List.of(cartInfo1, cartInfo2)).build();
        User user = User.builder()
                .id(userId)
                .shoppingCart(shoppingCart)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        List<ShoppingCartInfo> result = userService.getShoppingCartRecords(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getRecord().getTitle());
        assertEquals("Product 2", result.get(1).getRecord().getTitle());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void givenExistingUserWithEmptyShoppingCart_whenGetShoppingCartRecords_thenReturnEmptyList() {
        // Given
        UUID userId = UUID.randomUUID();
        ShoppingCart shoppingCart = ShoppingCart.builder().shoppingCartInfos(Collections.emptyList()).build();
        User user = User.builder()
                .id(userId)
                .shoppingCart(shoppingCart)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        List<ShoppingCartInfo> result = userService.getShoppingCartRecords(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void givenNonExistingUser_whenGetShoppingCartRecords_thenThrowUsernameNotFoundException() {
        // Given
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> userService.getShoppingCartRecords(userId));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void givenRecordInWishlist_whenRemoveFromWishlist_thenRemoveRecordAndSaveUser() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        Record record = Record.builder().id(recordId).build();

        User user = User.builder()
                .id(userId)
                .wishlist(new ArrayList<>(List.of(record)))
                .build();

        // When
        userService.removeFromWishlist(user, record);

        // Then
        assertTrue(user.getWishlist().isEmpty());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenRecordNotInWishlist_whenRemoveFromWishlist_thenDoNothing() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        Record record = Record.builder().id(recordId).build();

        User user = User.builder()
                .id(userId)
                .wishlist(new ArrayList<>())
                .build();

        // When
        userService.removeFromWishlist(user, record);

        // Then
        assertTrue(user.getWishlist().isEmpty());
        verify(userRepository, never()).save(user);
    }

    @Test
    void givenOrder_whenAddOrderToUserOrders_thenAddOrderAndSaveUser() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).build();
        User user = User.builder().id(userId).orders(new ArrayList<>()).build();
        // When
        userService.addOrderToUserOrders(order, user);

        // Then
        assertEquals(1, user.getOrders().size());
        assertEquals(orderId, user.getOrders().getFirst().getId());
        verify(userRepository, times(1)).save(user);
    }

    //    @Test
//    void givenShoppingCart_whenClearShoppingCart_thenClearCartAndSaveUser() {
//        // Given
//        UUID userId = UUID.randomUUID();
//        ShoppingCartInfo cartInfo = new ShoppingCartInfo(UUID.randomUUID(), Record.builder().title("Product 1").build(), null, 100);
//        ShoppingCart shoppingCart = ShoppingCart.builder().shoppingCartInfos(List.of(cartInfo)).totalQuantity(10).totalPrice(BigDecimal.ZERO).build();
//        User user = User.builder().id(userId).shoppingCart(shoppingCart).build();
//
//        // When
//        userService.clearShoppingCart(user);
//
//        // Then
//        assertTrue(user.getShoppingCart().getShoppingCartInfos().isEmpty());
//        assertEquals(0, user.getShoppingCart().getTotalQuantity());
//        assertEquals(BigDecimal.ZERO, user.getShoppingCart().getTotalPrice());
//        verify(userRepository, times(1)).save(user);
//    }
    @Test
    void givenPageable_whenGetEightUsers_thenReturnPageOfUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 8);
        User user1 = User.builder().id(UUID.randomUUID()).username("user1").build();
        User user2 = User.builder().id(UUID.randomUUID()).username("user2").build();
        User user3 = User.builder().id(UUID.randomUUID()).username("user3").build();
        User user4 = User.builder().id(UUID.randomUUID()).username("user4").build();
        User user5 = User.builder().id(UUID.randomUUID()).username("user5").build();
        User user6 = User.builder().id(UUID.randomUUID()).username("user6").build();
        User user7 = User.builder().id(UUID.randomUUID()).username("user7").build();
        User user8 = User.builder().id(UUID.randomUUID()).username("user8").build();

        Page<User> page = new PageImpl<>(List.of(user1, user2, user3, user4, user5, user6, user7, user8));

        when(userRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<User> result = userService.getEightUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(8, result.getContent().size());
        assertEquals("user1", result.getContent().get(0).getUsername());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void givenUserAndLoggedInAdmin_whenChangeUserRole_theCorrectRoleIsAssigned() {

        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.USER)
                .build();
        User loggedIn = User.builder()
                .id(UUID.randomUUID())
                .role(Role.ADMIN)
                .build();

        // When
        userService.switchUserRole(user, loggedIn);

        // Then
        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenAdminAndLoggedInAdmin_whenChangeUserRole_theCorrectRoleIsAssigned() {

        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.ADMIN)
                .build();
        User loggedIn = User.builder()
                .id(UUID.randomUUID())
                .role(Role.ADMIN)
                .build();

        // When
        userService.switchUserRole(user, loggedIn);

        // Then
        assertEquals(Role.USER, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserAndLoggedInUserAreTheSame_whenChangeUserRole_thenDoesNothing() {

        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(Role.USER)
                .build();
        User loggedIn = User.builder()
                .id(userId)
                .role(Role.USER)
                .build();

        // When
        userService.switchUserRole(user, loggedIn);

        // Then
        assertEquals(Role.USER, user.getRole());
        verify(userRepository, never()).save(user);
    }

    @Test
    void givenActiveUserAndLoggedInUser_whenChangeUserRole_thenCorrectStatusIsAssigned() {
        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).active(true).build();
        User loggedIn = User.builder().id(UUID.randomUUID()).active(true).build();

        //When
        when(statisticService.getStatisticsForToday()).thenReturn(Statistics.builder().activeUsers(1).inactiveUsers(0).build());
        userService.switchUserStatus(user, loggedIn);
        //Then
        assertFalse(user.isActive());
        assertEquals(0, statisticService.getStatisticsForToday().getActiveUsers());
        assertEquals(1, statisticService.getStatisticsForToday().getInactiveUsers());
        verify(statisticService, times(1)).save(any());
    }

    @Test
    void givenInactiveUserAndLoggedInUser_whenChangeUserRole_thenCorrectStatusIsAssigned() {
        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).active(false).build();
        User loggedIn = User.builder().id(UUID.randomUUID()).active(true).build();

        //When
        when(statisticService.getStatisticsForToday()).thenReturn(Statistics.builder().activeUsers(0).inactiveUsers(1).build());
        userService.switchUserStatus(user, loggedIn);
        //Then
        assertTrue(user.isActive());
        assertEquals(1, statisticService.getStatisticsForToday().getActiveUsers());
        assertEquals(0, statisticService.getStatisticsForToday().getInactiveUsers());
        verify(statisticService, times(1)).save(any());
    }

    @Test
    void givenUserAndLoggedInUserAreTheSame_whenChangeUserRole_thenDoesNothnig() {
        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).active(true).build();
        User loggedIn = User.builder().id(userId).active(true).build();

        //When
        when(statisticService.getStatisticsForToday()).thenReturn(Statistics.builder().activeUsers(0).inactiveUsers(1).build());
        userService.switchUserStatus(user, loggedIn);
        //Then
        assertTrue(user.isActive());
        assertEquals(0, statisticService.getStatisticsForToday().getActiveUsers());
        assertEquals(1, statisticService.getStatisticsForToday().getInactiveUsers());
        verify(statisticService, never()).save(any());
    }

    @Test
    void givenUserAndEditAccountRequest_whenEditInfo_thenShouldReturnUpdatedUser() {
        //Given
        EditAccountRequest request = EditAccountRequest.builder()
                .street("street")
                .city("city")
                .state("state")
                .zip("zip")
                .phone("phone")
                .firstName("firstName")
                .password("password")
                .lastName("lastName")
                .email("email")
                .urlPhoto("urlPhoto")
                .build();
        Address address = Address.builder()
                .zip("zip")
                .city("city")
                .state("state")
                .street("street")
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .address(address).build();
        //When
        when(addressService.save(any(), any(), any(), any())).thenReturn(address);
        when(userRepository.save(any())).thenReturn(user);
        userService.editInfo(user, request);

        //Then
        assertEquals("phone", user.getPhone());
        assertEquals("email", user.getEmail());
        assertEquals("firstName", user.getFirstName());
        assertEquals("lastName", user.getLastName());
        verify(addressService, times(1)).save(any(), any(), any(), any());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserAndEditAccountRequestWithOldPassword_whenEditInfo_thenShouldReturnUpdatedUser() {
        //Given
        EditAccountRequest request = EditAccountRequest.builder()
                .street("street")
                .city("city")
                .state("state")
                .zip("zip")
                .phone("phone")
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .urlPhoto("urlPhoto")
                .build();
        Address address = Address.builder()
                .zip("zip")
                .city("city")
                .state("state")
                .street("street")
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .password("$2b$12$da71Fv8fxpvl0FcRId14NO4WnPKmEGPVE/xMhKPa4/5LNIVp7GIR2")
                .active(true)
                .address(address).build();
        //When
        when(addressService.save(any(), any(), any(), any())).thenReturn(address);
        when(userRepository.save(any())).thenReturn(user);
        userService.editInfo(user, request);

        //Then
        assertEquals("phone", user.getPhone());
        assertEquals("email", user.getEmail());
        assertEquals("firstName", user.getFirstName());
        assertEquals("lastName", user.getLastName());
        assertEquals("$2b$12$da71Fv8fxpvl0FcRId14NO4WnPKmEGPVE/xMhKPa4/5LNIVp7GIR2", user.getPassword());
        verify(addressService, times(1)).save(any(), any(), any(), any());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserAndEditAccountRequestWithOldPhotoUrl_whenEditInfo_thenShouldReturnUpdatedUser() {
        //Given
        EditAccountRequest request = EditAccountRequest.builder()
                .street("street")
                .city("city")
                .state("state")
                .zip("zip")
                .phone("phone")
                .firstName("firstName")
                .lastName("lastName")
                .password("password")
                .email("email")
                .build();
        Address address = Address.builder()
                .zip("zip")
                .city("city")
                .state("state")
                .street("street")
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .profilePicture("urlPhoto")
                .address(address).build();
        //When
        when(addressService.save(any(), any(), any(), any())).thenReturn(address);
        when(userRepository.save(any())).thenReturn(user);
        userService.editInfo(user, request);

        //Then
        assertEquals("phone", user.getPhone());
        assertEquals("email", user.getEmail());
        assertEquals("firstName", user.getFirstName());
        assertEquals("lastName", user.getLastName());
        assertEquals("urlPhoto", user.getProfilePicture());
        verify(addressService, times(1)).save(any(), any(), any(), any());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserAndEditAccountRequestWithOldPhotoUrlAndOldPassword_whenEditInfo_thenShouldReturnUpdatedUser() {
        //Given
        EditAccountRequest request = EditAccountRequest.builder()
                .street("street")
                .city("city")
                .state("state")
                .zip("zip")
                .phone("phone")
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .build();
        Address address = Address.builder()
                .zip("zip")
                .city("city")
                .state("state")
                .street("street")
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .password("$2b$12$da71Fv8fxpvl0FcRId14NO4WnPKmEGPVE/xMhKPa4/5LNIVp7GIR2")
                .profilePicture("urlPhoto")
                .address(address).build();
        //When
        when(addressService.save(any(), any(), any(), any())).thenReturn(address);
        when(userRepository.save(any())).thenReturn(user);
        userService.editInfo(user, request);

        //Then
        assertEquals("phone", user.getPhone());
        assertEquals("email", user.getEmail());
        assertEquals("firstName", user.getFirstName());
        assertEquals("lastName", user.getLastName());
        assertEquals("urlPhoto", user.getProfilePicture());
        assertEquals("$2b$12$da71Fv8fxpvl0FcRId14NO4WnPKmEGPVE/xMhKPa4/5LNIVp7GIR2", user.getPassword());
        verify(addressService, times(1)).save(any(), any(), any(), any());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUser_whenDeleteUser_thenShouldReturnInactiveUser() {
        //Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .build();
        Statistics statistics = Statistics.builder()
                .inactiveUsers(0)
                .activeUsers(1)
                .build();
        //When
        when(statisticService.getStatisticsForToday()).thenReturn(statistics);

        userService.deleteUser(user);

        //Then
        assertFalse(user.isActive());
        assertEquals(1, statistics.getInactiveUsers());
        assertEquals(0, statistics.getActiveUsers());
        verify(statisticService, times(1)).getStatisticsForToday();
        verify(statisticService, times(1)).save(any());
        verify(userRepository, times(1)).save(user);

    }

    // public User getUserByEmail(String receiver) {
    //        return userRepository.findByEmail(receiver).orElseThrow(()->new UsernameNotFoundException("User not found"));
    //    }

    @Test
    void givenReceiver_whenGetUserByEmail_thenShouldReturnUser() {
        //Given
        String receiver = "username";
        User user = User.builder().email(receiver).build();

        //When
        when(userRepository.findByEmail(receiver)).thenReturn(Optional.of(user));

        userService.getUserByEmail(receiver);

        //Then
        assertNotNull(user);
    }

    @Test
    void givenReceiver_whenGetUserByEmail_thenShouldThrowException() {
        //Given
        String receiver = "username";

        //When and Then
        when(userRepository.findByEmail(receiver)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserByEmail(receiver));
    }

    @Test
    void givenUser_whenGetTotalSpentMoneyByGivenUser_thenShouldReturnTotalSpentMoney() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .orders(List.of(Order.builder().totalPrice(BigDecimal.valueOf(20)).build()))
                .build();
        BigDecimal result = BigDecimal.valueOf(20);
        userService.getTotalSpentMoneyByGivenUser(user);
        assertEquals(result, userService.getTotalSpentMoneyByGivenUser(user));
    }

    @Test
    void givenUsersWithOrders_whenGetTopCustomer_thenReturnUserWithHighestSpent() {
        // Given
        User user1 = createUserWithOrders(3, new BigDecimal("100.00"));
        User user2 = createUserWithOrders(5, new BigDecimal("200.00"));
        User user3 = createUserWithOrders(2, new BigDecimal("200.00"));
        List<User> users = List.of(user1, user2, user3);
        when(userRepository.findAll()).thenReturn(users);

        // When
        User topCustomer = userService.getTopCustomer();

        // Then
        assertNotNull(topCustomer);
        assertEquals(user2.getId(), topCustomer.getId());
    }

    @Test
    void givenUsersWithOrdersThatHaveSameTotalPrice_whenGetTopCustomer_thenReturnUserWithHighestSpentAndHighestOrders() {
        // Given
        User user1 = createUserWithOrders(10, new BigDecimal("100.00"));
        User user2 = createUserWithOrders(5, new BigDecimal("200.00"));
        User user3 = createUserWithOrders(2, new BigDecimal("200.00"));
        List<User> users = List.of(user1, user2, user3);
        when(userRepository.findAll()).thenReturn(users);

        // When
        User topCustomer = userService.getTopCustomer();

        // Then
        assertNotNull(topCustomer);
        assertEquals(user1.getId(), topCustomer.getId());
    }
    @Test
    void givenNoUsers_whenGetTopCustomer_thenReturnNull() {
        // Given
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        User topCustomer = userService.getTopCustomer();

        // Then
        assertNull(topCustomer);
    }
    @Test
    void givenUsersWithoutOrders_whenGetTopCustomer_thenReturnNull() {
        // Given
        User user1 = createUserWithOrders(0, BigDecimal.ZERO);
        User user2 = createUserWithOrders(0, BigDecimal.ZERO);
        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        // When
        User topCustomer = userService.getTopCustomer();

        // Then
        assertNull(topCustomer);
    }

    private User createUserWithOrders(int numberOfOrders, BigDecimal totalSpent) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .orders(new ArrayList<>())
                .build();

        for (int i = 0; i < numberOfOrders; i++) {
            Order order = Order.builder()
                    .id(UUID.randomUUID())
                    .totalPrice(totalSpent)
                    .build();
            user.getOrders().add(order);
        }
        return user;
    }
    @Test
    void givenEmptyCart_whenAddRecordToCart_thenAddNewRecord() {
        // Given
        UUID userId = UUID.randomUUID();
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("20.00"))
                .build();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .shoppingCartInfos(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .totalQuantity(0)
                .build();
        User user = User.builder()
                .id(userId)
                .shoppingCart(shoppingCart)
                .build();

        ShoppingCartInfo cartInfo = ShoppingCartInfo.builder()
                .record(record)
                .shoppingCart(shoppingCart)
                .quantity(1)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(shoppingCartInfoService.addNewProductInShoppingCart(record, shoppingCart)).thenReturn(cartInfo);

        // When
        userService.addRecordToCart(record, userId);

        // Then
        assertEquals(1, user.getShoppingCart().getShoppingCartInfos().size());
        assertEquals(record.getId(), user.getShoppingCart().getShoppingCartInfos().get(0).getRecord().getId());

        verify(userRepository, times(1)).save(user);
    }
    @Test
    void givenExistingRecordInCart_whenAddRecordToCart_thenIncreaseQuantity() {
        // Given
        UUID userId = UUID.randomUUID();
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("20.00"))
                .build();

        ShoppingCartInfo cartInfo = ShoppingCartInfo.builder()
                .record(record)
                .quantity(1)
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .shoppingCartInfos(new ArrayList<>())
                .totalPrice(new BigDecimal("20.00"))
                .totalQuantity(1)
                .build();

        shoppingCart.getShoppingCartInfos().add(cartInfo);

        User user = User.builder()
                .id(userId)
                .shoppingCart(shoppingCart)
                .build();

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        // When
        userService.addRecordToCart(record, userId);

        // Then
        assertEquals(2, cartInfo.getQuantity());
        assertEquals(2, shoppingCart.getTotalQuantity());
        assertEquals(new BigDecimal("40.00"), shoppingCart.getTotalPrice());

        verify(userRepository, times(1)).save(user);
    }
    @Test
    void givenUserWithoutCart_whenAddRecordToCart_thenCreateCartAndAddRecord() {
        // Given
        UUID userId = UUID.randomUUID();
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("20.00"))
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .shoppingCartInfos(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .totalQuantity(0)
                .build();

        User user = User.builder()
                .id(userId)
                .shoppingCart(null)
                .build();

        ShoppingCartInfo cartInfo = ShoppingCartInfo.builder()
                .record(record)
                .quantity(1)
                .build();

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(shoppingCartService.createShoppingCartForUser(user)).thenReturn(shoppingCart);
        when(shoppingCartInfoService.addNewProductInShoppingCart(record, shoppingCart)).thenReturn(cartInfo);

        // When
        userService.addRecordToCart(record, userId);

        // Then
        assertNotNull(user.getShoppingCart());
        assertEquals(1, user.getShoppingCart().getShoppingCartInfos().size());
        assertEquals(record.getId(), user.getShoppingCart().getShoppingCartInfos().get(0).getRecord().getId());

        verify(userRepository, times(1)).save(user);
    }
}
