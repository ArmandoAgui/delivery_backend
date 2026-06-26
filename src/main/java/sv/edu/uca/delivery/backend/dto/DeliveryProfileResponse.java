package sv.edu.uca.delivery.backend.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryProfileResponse(
        UUID deliveryUserId,
        String deliveryUserName,
        boolean available,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime locationRecordedAt,
        Double averageRating,
        long reviewCount
) {
}
