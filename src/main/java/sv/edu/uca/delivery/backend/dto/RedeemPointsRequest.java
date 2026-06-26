package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RedeemPointsRequest(
        @NotNull @Min(1) Integer points
) {
}
