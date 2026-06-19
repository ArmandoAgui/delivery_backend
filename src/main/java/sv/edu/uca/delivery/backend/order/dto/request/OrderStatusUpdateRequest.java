package sv.edu.uca.delivery.backend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;

public record OrderStatusUpdateRequest(

        @NotNull(message = "El estado es obligatorio")
        OrderStatus status
) {}