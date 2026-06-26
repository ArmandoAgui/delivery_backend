package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.NotNull;
import sv.edu.uca.delivery.backend.entity.DeliveryStatus;

public record UpdateDeliveryStatusRequest(
        @NotNull DeliveryStatus status
) {
}
