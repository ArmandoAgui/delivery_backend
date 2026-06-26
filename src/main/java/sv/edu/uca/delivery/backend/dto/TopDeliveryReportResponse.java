package sv.edu.uca.delivery.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopDeliveryReportResponse(
        UUID deliveryUserId,
        String deliveryUserName,
        long deliveries,
        BigDecimal earnings
) {
}
