package sv.edu.uca.delivery.backend.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantUpdateDTO {

    @NotBlank
    private String name;

    private boolean open;

    private boolean active;
}