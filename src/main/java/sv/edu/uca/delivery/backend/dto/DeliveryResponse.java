package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponse(
        UUID id,
        UUID orderId,
        UUID deliveryUserId,
        String deliveryUserName,
        DeliveryStatus status,
        OrderStatus orderStatus,
        String restaurantName,
        String restaurantAddress,
        String deliveryAddress,
        String orderSummary,
        BigDecimal distanceKm,
        BigDecimal deliveryFee,
        BigDecimal tipAmount,
        BigDecimal totalAmount,
        LocalDateTime assignedAt,
        LocalDateTime pickedUpAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt
) {
    public DeliveryResponse(
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
        this(
                id,
                orderId,
                deliveryUserId,
                deliveryUserName,
                status,
                orderStatus,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                assignedAt,
                pickedUpAt,
                deliveredAt,
                createdAt
        );
    }
}
