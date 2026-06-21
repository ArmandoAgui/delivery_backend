package sv.edu.uca.delivery.backend.user.mapper;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;
import sv.edu.uca.delivery.backend.user.entity.User;

/**
 * Convierte entidades User a DTOs de respuesta.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getName().name())
                .build();
    }
}
