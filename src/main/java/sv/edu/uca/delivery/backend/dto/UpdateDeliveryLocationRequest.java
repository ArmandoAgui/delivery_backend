package sv.edu.uca.delivery.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateDeliveryLocationRequest(
        @Schema(example = "13.692900", description = "Latitud del repartidor. Se normaliza a 6 decimales.")
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") @Digits(integer = 2, fraction = 8) BigDecimal latitude,
        @Schema(example = "-89.218200", description = "Longitud del repartidor. Se normaliza a 6 decimales.")
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") @Digits(integer = 3, fraction = 8) BigDecimal longitude,
        Boolean available
) {
}
