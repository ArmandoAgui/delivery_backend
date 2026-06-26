package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.NotNull;
import sv.edu.uca.delivery.backend.entity.OrderStatus;

public record OrderStatusUpdateRequest(

        @NotNull(message = "El estado es obligatorio")
        OrderStatus status
) {}