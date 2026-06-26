package sv.edu.uca.delivery.backend.mapper;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.dto.UserResponse;
import sv.edu.uca.delivery.backend.entity.User;

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
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
