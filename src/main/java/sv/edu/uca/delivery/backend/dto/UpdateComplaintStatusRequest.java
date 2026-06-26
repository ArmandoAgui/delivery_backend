package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import sv.edu.uca.delivery.backend.entity.ComplaintStatus;

import java.math.BigDecimal;

public record UpdateComplaintStatusRequest(
        @NotNull ComplaintStatus status,
        @Size(max = 1000) String resolution,
        RefundType refundType,
        @Positive BigDecimal refundAmount
) {
    public UpdateComplaintStatusRequest(ComplaintStatus status) {
        this(status, null, null, null);
    }
}
