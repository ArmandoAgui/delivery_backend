package sv.edu.uca.delivery.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Size(max = 30, message = "Phone cannot exceed 30 characters")
    private String phone;

    @Size(min = 8, max = 120, message = "Password must be between 8 and 120 characters")
    private String password;

    private RoleName role;
}
