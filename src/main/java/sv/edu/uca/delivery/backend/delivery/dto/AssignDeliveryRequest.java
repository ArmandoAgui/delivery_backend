package sv.edu.uca.delivery.backend.delivery.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDeliveryRequest(
        @NotNull UUID orderId
) {
}
