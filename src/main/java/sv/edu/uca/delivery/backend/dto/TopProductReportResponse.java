package sv.edu.uca.delivery.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopProductReportResponse(
        UUID productId,
        String productName,
        String restaurantName,
        long quantitySold,
        BigDecimal revenue
) {
}
