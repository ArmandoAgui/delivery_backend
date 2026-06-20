package sv.edu.uca.delivery.backend.payment.dto;

import sv.edu.uca.delivery.backend.order.dto.response.OrderResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record PaypalOrderResponse(
        String id,
        UUID orderId,
        String status,
        String approvalUrl,
        BigDecimal amount,
        String currency,
        OrderResponse order
) {
}
