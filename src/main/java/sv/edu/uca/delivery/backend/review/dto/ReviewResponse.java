package sv.edu.uca.delivery.backend.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID orderId,
        UUID reviewerUserId,
        UUID restaurantId,
        UUID deliveryUserId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
