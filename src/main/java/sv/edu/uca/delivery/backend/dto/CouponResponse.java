package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        BigDecimal maxDiscountAmount,
        Integer usageLimit,
        Integer usedCount,
        LocalDateTime startsAt,
        LocalDateTime expiresAt,
        boolean active
) {
}
