package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.OrderStatus;
import sv.edu.uca.delivery.backend.entity.PaymentStatus;

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
        Integer estimatedDeliveryMinutes,
        BigDecimal demandMultiplier,
        Boolean peakDemand,
        BigDecimal distanceKm,
        PaymentStatus paymentStatus,
        String refundStatus,
        String statusReason,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistory
) {}
