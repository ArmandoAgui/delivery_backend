package sv.edu.uca.delivery.backend.delivery.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryAvailabilityRequest(
        @NotNull Boolean available
) {
}
