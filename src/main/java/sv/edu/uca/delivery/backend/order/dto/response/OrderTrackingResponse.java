package sv.edu.uca.delivery.backend.order.dto.response;

import sv.edu.uca.delivery.backend.order.entity.OrderStatus;

import java.util.List;
import java.util.UUID;

public record OrderTrackingResponse(
        UUID orderId,
        OrderStatus status,
        String restaurantName,
        String deliveryAddress,
        List<OrderStatusHistoryResponse> history
) {
}
