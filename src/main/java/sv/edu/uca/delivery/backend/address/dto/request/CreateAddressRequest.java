package sv.edu.uca.delivery.backend.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAddressRequest {

    private String label;

    @NotBlank
    private String streetAddress;

    @NotBlank
    private String city;

    private String state;

    @NotBlank
    private String country;

    private String postalCode;

    private boolean defaultAddress;
}