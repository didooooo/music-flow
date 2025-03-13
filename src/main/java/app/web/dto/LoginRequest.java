package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class LoginRequest {
    @Size(min = 3, message = "Username must be at least 6 symbols")
    private String username;

    @Size(min = 3, message = "Password must be at least 6 symbols")
    private String password;
}
