package sv.edu.uca.delivery.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommissionResponse(
        Long id,
        UUID restaurantId,
        BigDecimal commissionPercentage,
        BigDecimal deliveryCommissionPercentage,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        boolean global
) {
    public CommissionResponse(
            Long id,
            UUID restaurantId,
            BigDecimal commissionPercentage,
            BigDecimal deliveryCommissionPercentage,
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {
        this(id, restaurantId, commissionPercentage, deliveryCommissionPercentage, startsAt, endsAt, restaurantId == null);
    }
}
