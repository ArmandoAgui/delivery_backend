package sv.edu.uca.delivery.backend.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RestaurantCreateDTO {

    @NotNull
    private UUID ownerId;

    @NotBlank
    private String name;

    private boolean open;
}