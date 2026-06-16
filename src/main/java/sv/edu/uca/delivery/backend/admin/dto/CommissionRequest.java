package sv.edu.uca.delivery.backend.admin.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommissionRequest(
        @NotNull UUID restaurantId,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal commissionPercentage,
        @NotNull LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}
