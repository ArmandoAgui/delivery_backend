package sv.edu.uca.delivery.backend.delivery.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryProfileResponse(
        UUID deliveryUserId,
        String deliveryUserName,
        boolean available,
        Double latitude,
        Double longitude,
        LocalDateTime locationRecordedAt
) {
}
