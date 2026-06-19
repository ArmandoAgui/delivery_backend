package sv.edu.uca.delivery.backend.delivery.dto;

import jakarta.validation.constraints.NotNull;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;

public record UpdateDeliveryStatusRequest(
        @NotNull DeliveryStatus status
) {
}
