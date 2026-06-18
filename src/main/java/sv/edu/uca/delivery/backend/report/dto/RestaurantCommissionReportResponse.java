package sv.edu.uca.delivery.backend.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RestaurantCommissionReportResponse(
        UUID restaurantId,
        String restaurantName,
        long orders,
        BigDecimal revenue,
        BigDecimal commissionPercentage,
        BigDecimal commissionAmount
) {
}
