package sv.edu.uca.delivery.backend.auth.dto;

import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
