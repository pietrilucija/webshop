package hr.algebra.webshop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email je obavezan")
    @Email
    private String email;

    @NotBlank(message = "Lozinka je obavezna")
    private String password;
}