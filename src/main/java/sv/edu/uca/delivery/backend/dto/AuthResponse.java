package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
