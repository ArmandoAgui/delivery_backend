package sv.edu.uca.delivery.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartQuoteResponse(
        UUID cartId,
        UUID restaurantId,
        String restaurantName,
        BigDecimal subtotalAmount,
        BigDecimal deliveryFee,
        BigDecimal tipAmount,
        BigDecimal discountAmount,
        BigDecimal digitalWalletApplied,
        BigDecimal totalAmount,
        String couponCode,
        boolean couponApplied,
        String couponMessage,
        Integer estimatedDeliveryMinutes,
        Boolean peakDemand,
        BigDecimal distanceKm
) {
}
