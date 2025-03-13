package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
    @NotBlank
    @Size(min = 3, max = 20)
    private String password;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 3, max = 20)
    private String phone;
    private String profilePicture;
    @NotBlank
    @Size(min = 3, max = 20)
    private String street;
    @NotBlank
    @Size(min = 3, max = 30)
    private String city;
    private String state;
    @NotBlank
    @Size(min = 3, max = 20)
    private String zip;
}
