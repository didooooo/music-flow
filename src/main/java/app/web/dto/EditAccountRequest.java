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
public class EditAccountRequest {
    private String firstName;
    private String lastName;
    @NotBlank
    @Email
    private String email;
    private String password;
    @NotBlank
    @Size(min = 3, max = 20)
    private String phone;
    @NotBlank
    @Size(min = 3, max = 20)
    private String city;
    private String state;
    @NotBlank
    @Size(min = 3, max = 30)
    private String street;
    @NotBlank
    @Size(min = 3, max = 20)
    private String zip;
    private String urlPhoto;

}
