package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.ComplaintStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ComplaintResponse(
        UUID id,
        UUID orderId,
        UUID customerUserId,
        String customerName,
        String customerEmail,
        UUID restaurantId,
        String restaurantName,
        String orderStatus,
        ComplaintStatus status,
        String subject,
        String description,
        String resolution,
        LocalDateTime createdAt,
        RefundResponse refund
) {
    public ComplaintResponse(
            UUID id,
            UUID orderId,
            UUID customerUserId,
            ComplaintStatus status,
            String subject,
            String description,
            String resolution,
            LocalDateTime createdAt,
            RefundResponse refund
    ) {
        this(id, orderId, customerUserId, null, null, null, null, null, status, subject, description, resolution, createdAt, refund);
    }
}
