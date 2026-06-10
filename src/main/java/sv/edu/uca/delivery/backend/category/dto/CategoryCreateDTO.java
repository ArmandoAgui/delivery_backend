package sv.edu.uca.delivery.backend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryCreateDTO {

    @NotNull
    private UUID restaurantId;

    @NotBlank
    private String name;

    private String description;
}