package sv.edu.uca.delivery.backend.delivery.dto;

import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponse(
        UUID id,
        UUID orderId,
        UUID deliveryUserId,
        String deliveryUserName,
        DeliveryStatus status,
        OrderStatus orderStatus,
        LocalDateTime assignedAt,
        LocalDateTime pickedUpAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt
) {
}
