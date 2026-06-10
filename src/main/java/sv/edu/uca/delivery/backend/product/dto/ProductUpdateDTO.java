package sv.edu.uca.delivery.backend.product.dto;

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

    private String description;

    @NotNull
    private BigDecimal price;

    private UUID categoryId;

    //@NotNull
    //private ProductCategory category;

    private boolean available;
}
