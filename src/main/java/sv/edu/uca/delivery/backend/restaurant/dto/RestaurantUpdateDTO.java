package sv.edu.uca.delivery.backend.restaurant.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantUpdateDTO {

    @NotBlank
    private String name;

    private String description;

    @Size(max = 30)
    private String phone;

    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    private String streetAddress;

    @NotBlank
    private String city;

    private String state;

    @NotBlank
    private String country;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    private boolean open;
}
