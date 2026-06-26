package sv.edu.uca.delivery.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
