package sv.edu.uca.delivery.backend.complaint.dto;

import jakarta.validation.constraints.NotNull;
import sv.edu.uca.delivery.backend.complaint.entity.ComplaintStatus;

public record UpdateComplaintStatusRequest(
        @NotNull ComplaintStatus status
) {
}
