package sv.edu.uca.delivery.backend.address.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO utilizado para retornar información
 * de una dirección al cliente.
 */
@Data
@Builder
public class AddressResponse {

    private UUID id;

    private String label;

    private String streetAddress;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private boolean defaultAddress;
}