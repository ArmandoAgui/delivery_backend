package sv.edu.uca.delivery.backend.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommissionResponse(
        Long id,
        UUID restaurantId,
        BigDecimal commissionPercentage,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}
