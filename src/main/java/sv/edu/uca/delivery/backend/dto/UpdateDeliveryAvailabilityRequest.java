package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryAvailabilityRequest(
        @NotNull Boolean available
) {
}
