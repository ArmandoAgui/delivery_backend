package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemRequest(

        @NotNull(message = "El producto es obligatorio")
        UUID productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        Integer quantity
) {}