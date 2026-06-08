package sv.edu.uca.delivery.backend.order.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(

        @NotNull(message = "El restaurante es obligatorio")
        UUID restaurantId,

        @NotNull(message = "La dirección de entrega es obligatoria")
        UUID deliveryAddressId,

        @NotNull(message = "Los productos son obligatorios")
        @Size(min = 1, message = "El pedido debe tener al menos un producto")
        @Valid
        List<OrderItemRequest> items,

        // Opcional... el cliente puede o no mandar propina
        @DecimalMin(value = "0.0", message = "La propina no puede ser negativa")
        BigDecimal tipAmount,

        // código de cupón...
        String couponCode
) {}