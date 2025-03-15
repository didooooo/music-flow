package app.web.converters;

import app.user.model.Role;
import app.user.model.User;
import app.web.dto.RegisterRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RegisterRequestToUser implements Converter<RegisterRequest, User> {
    @Override
    public User convert(RegisterRequest source) {
        return User.builder()
                .profilePicture(source.getProfilePicture())
                .email(source.getEmail())
                .phone(source.getPhone())
                .username(source.getUsername())
                .role(Role.USER)
                .active(true)
                .build();
    }
}
