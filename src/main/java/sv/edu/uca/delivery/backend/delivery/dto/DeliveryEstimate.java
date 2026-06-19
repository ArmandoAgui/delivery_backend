package sv.edu.uca.delivery.backend.delivery.dto;

import java.math.BigDecimal;

public record DeliveryEstimate(
        BigDecimal estimatedDeliveryFee,
        Integer estimatedDeliveryMinutes,
        boolean peakDemand,
        BigDecimal distanceKm,
        BigDecimal demandMultiplier
) {
}
