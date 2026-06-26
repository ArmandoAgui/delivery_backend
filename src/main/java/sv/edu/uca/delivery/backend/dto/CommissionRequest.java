package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommissionRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal commissionPercentage,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal deliveryCommissionPercentage,
        @NotNull LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}
