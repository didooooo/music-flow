package app.user.service;

import app.address.model.Address;
import app.address.service.AddressService;
import app.security.AuthUser;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ConversionService conversionService;
    private final PasswordEncoder passwordEncoder;
    private final AddressService addressService;

    public UserService(UserRepository userRepository, ConversionService conversionService, PasswordEncoder passwordEncoder, AddressService addressService) {
        this.userRepository = userRepository;
        this.conversionService = conversionService;
        this.passwordEncoder = passwordEncoder;
        this.addressService = addressService;
    }
    @Transactional
    public User registerUser(RegisterRequest input) {
        Optional<User> userWithUsername = userRepository.findByUsername(input.getUsername());
        if(userWithUsername.isPresent()) {
            throw new RuntimeException("Username is already in use");
        }
        Optional<User> userWithEmail = userRepository.findByEmail(input.getEmail());
        if(userWithEmail.isPresent()) {
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
        return userRepository.findById(userId).orElseThrow(()-> new RuntimeException("There is no such user with [%s] ".formatted(userId)));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("There is no such user with [%s] ".formatted(username)));
        return new AuthUser(user.getId(),username,user.getPassword(),user.getRole(),user.isActive());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
