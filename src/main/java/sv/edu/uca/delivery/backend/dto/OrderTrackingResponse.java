package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.OrderStatus;
import sv.edu.uca.delivery.backend.entity.PaymentStatus;

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
        PaymentStatus paymentStatus,
        String refundStatus,
        String statusReason,
        List<OrderStatusHistoryResponse> history
) {
}
