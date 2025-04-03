package app;

import app.address.model.Address;
import app.address.repository.AddressRepository;
import app.config.TestConfig;
import app.exception.EmailAlreadyExistException;
import app.exception.UsernameAlreadyExistException;
import app.statistics.repository.StatisticsRepository;
import app.statistics.service.StatisticService;
import app.user.model.User;
import app.statistics.model.Statistics;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
@Import(TestConfig.class)
public class UserRegisterITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;
    @Autowired
    private StatisticService statisticService;

    @Test
    void registerUser_happyPath() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("testUser")
                .password("testPass123")
                .phone("088888")
                .email("test@example.com")
                .street("123 Test St")
                .city("Testville")
                .state("TS")
                .zip("12345")
                .build();

        // When
        User registeredUser = userService.registerUser(request);

        // Then
        Optional<User> dbUser = userRepository.findById(registeredUser.getId());
        assertTrue(dbUser.isPresent());
        assertEquals("testUser", dbUser.get().getUsername());
        assertEquals("test@example.com", dbUser.get().getEmail());

        Optional<Address> userAddress = addressRepository.findById(dbUser.get().getAddress().getId());
        assertTrue(userAddress.isPresent());
        assertEquals("123 Test St", userAddress.get().getStreet());

        assertNotEquals("testPass123", dbUser.get().getPassword());

        Statistics stats = statisticService.getStatisticsForToday();
        assertEquals(1, stats.getActiveUsers());
        assertEquals(1, stats.getTotalCustomers());
    }

    @Test
    void registerUser_withExistingUsername_shouldThrowException() {
        // Given
        RegisterRequest firstRequest = RegisterRequest.builder()
                .username("existingUser")
                .password("pass123")
                .email("first@example.com")
                .phone("088888")
                .street("123 Test St")
                .city("Testville")
                .state("TS")
                .zip("12345")
                .build();

        userService.registerUser(firstRequest);

        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("existingUser")
                .password("differentPass")
                .email("second@example.com")
                .build();

        // When/Then
        assertThrows(UsernameAlreadyExistException.class,
                () -> userService.registerUser(duplicateRequest));
    }

    @Test
    void registerUser_withExistingEmail_shouldThrowException() {
        // Given
        RegisterRequest firstRequest = RegisterRequest.builder()
                .username("user1")
                .password("pass123")
                .email("existing@example.com")
                .phone("088888")
                .street("123 Test St")
                .city("Testville")
                .state("TS")
                .zip("12345")
                .build();

        userService.registerUser(firstRequest);

        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("user2")
                .password("differentPass")
                .email("existing@example.com")
                .build();

        // When/Then
        assertThrows(EmailAlreadyExistException.class,
                () -> userService.registerUser(duplicateRequest));
    }
}
