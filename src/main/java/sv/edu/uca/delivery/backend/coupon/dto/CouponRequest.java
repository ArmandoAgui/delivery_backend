package sv.edu.uca.delivery.backend.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import sv.edu.uca.delivery.backend.coupon.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(
        @NotBlank String code,
        String description,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        @NotNull @PositiveOrZero BigDecimal minimumOrderAmount,
        @Positive BigDecimal maxDiscountAmount,
        @Positive Integer usageLimit,
        @NotNull LocalDateTime startsAt,
        @NotNull LocalDateTime expiresAt,
        boolean active
) {
}
