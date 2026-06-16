package sv.edu.uca.delivery.backend.order.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        Integer quantity,
        BigDecimal lineTotal
) {}