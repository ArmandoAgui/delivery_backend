package sv.edu.uca.delivery.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String role;

    private boolean active;

    private LocalDateTime createdAt;
}
