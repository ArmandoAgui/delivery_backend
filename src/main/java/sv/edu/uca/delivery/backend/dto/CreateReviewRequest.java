package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import sv.edu.uca.delivery.backend.entity.ReviewType;

import java.util.UUID;

public record CreateReviewRequest(
        @NotNull UUID orderId,
        ReviewType reviewType,
        UUID productId,
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment
) {
}
