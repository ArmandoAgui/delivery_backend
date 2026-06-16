package sv.edu.uca.delivery.backend.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        UUID restaurantId,
        String restaurantName,
        BigDecimal subtotal,
        List<CartItemResponse> items
) {
}
