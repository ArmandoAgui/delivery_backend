package sv.edu.uca.delivery.backend.delivery.dto;

import java.math.BigDecimal;

public record DeliveryStatsResponse(
        long pendingRequests,
        long activeDeliveries,
        long completedDeliveries,
        long rejectedRequests,
        BigDecimal estimatedDeliveryEarnings,
        BigDecimal tipsReceived
) {
}
