package sv.edu.uca.delivery.backend.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateComplaintRequest(
        @NotNull UUID orderId,
        @NotBlank @Size(max = 150) String subject,
        @NotBlank String description
) {
}
