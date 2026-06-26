package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductUpdateDTO {

    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;

    private String description;

    @NotNull
    private BigDecimal price;

    private boolean available;
}
