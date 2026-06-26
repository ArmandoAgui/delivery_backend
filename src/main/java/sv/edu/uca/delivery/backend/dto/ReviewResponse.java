package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.ReviewType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID orderId,
        UUID reviewerUserId,
        UUID restaurantId,
        UUID deliveryUserId,
        UUID productId,
        String productName,
        ReviewType reviewType,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
