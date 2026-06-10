package sv.edu.uca.delivery.backend.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateDTO {

    @NotBlank
    private String name;

    private String description;
}