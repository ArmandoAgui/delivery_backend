package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryResponse(
        UUID id,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant changedAt,
        String notes
) {}
