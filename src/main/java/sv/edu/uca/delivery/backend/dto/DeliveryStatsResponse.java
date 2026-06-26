package sv.edu.uca.delivery.backend.dto;

import java.math.BigDecimal;

public record DeliveryStatsResponse(
        long pendingRequests,
        long activeDeliveries,
        long completedDeliveries,
        long rejectedRequests,
        BigDecimal estimatedDeliveryEarnings,
        BigDecimal tipsReceived,
        BigDecimal platformCommissionPercentage,
        BigDecimal grossEarnings,
        BigDecimal platformCommissionAmount,
        BigDecimal netEarnings
) {
}
