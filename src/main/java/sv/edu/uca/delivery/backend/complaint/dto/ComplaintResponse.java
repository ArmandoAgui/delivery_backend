package sv.edu.uca.delivery.backend.complaint.dto;

import sv.edu.uca.delivery.backend.complaint.entity.ComplaintStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ComplaintResponse(
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
}
