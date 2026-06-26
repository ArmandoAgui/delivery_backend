package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PromotionCreateDTO {

    @NotNull
    private UUID restaurantId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer discountPercentage;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}