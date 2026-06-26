package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RefundResponse(
        UUID id,
        boolean refundRequested,
        RefundStatus refundStatus,
        BigDecimal amount,
        String reason,
        LocalDateTime processedAt
) {
}
