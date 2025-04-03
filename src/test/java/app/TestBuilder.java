package app;

import app.address.model.Address;
import app.security.AuthUser;
import app.user.model.Role;
import app.user.model.User;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

import static app.user.model.Role.ADMIN;
import static app.user.model.Role.USER;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        return User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123123")
                .address(new Address(UUID.randomUUID(), "street", "city", "state", "zip"))
                .role(Role.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static AuthUser aRandomAuthUser() {
        return new AuthUser(UUID.randomUUID(), "User123", "123123", USER, true);
    }

    public static AuthUser aRandomAuthAdmin() {
        return new AuthUser(UUID.randomUUID(), "User123", "123123", ADMIN, true);
    }
}
