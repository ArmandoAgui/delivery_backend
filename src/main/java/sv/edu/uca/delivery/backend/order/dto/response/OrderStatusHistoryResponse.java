package sv.edu.uca.delivery.backend.order.dto.response;

import sv.edu.uca.delivery.backend.order.entity.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryResponse(
        UUID id,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant changedAt
) {}