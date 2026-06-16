package sv.edu.uca.delivery.backend.order.dto.response;

import sv.edu.uca.delivery.backend.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        UUID restaurantId,
        UUID deliveryAddressId,
        OrderStatus status,
        BigDecimal subtotalAmount,
        BigDecimal taxAmount,
        BigDecimal deliveryFee,
        BigDecimal tipAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistory
) {}
