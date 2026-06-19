package sv.edu.uca.delivery.backend.order.dto.response;

import sv.edu.uca.delivery.backend.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderTrackingResponse(
        UUID orderId,
        OrderStatus status,
        String restaurantName,
        String deliveryAddress,
        String deliveryStatus,
        UUID deliveryUserId,
        String deliveryUserName,
        Integer estimatedDeliveryMinutes,
        BigDecimal deliveryFee,
        BigDecimal distanceKm,
        Boolean peakDemand,
        List<OrderStatusHistoryResponse> history
) {
}
