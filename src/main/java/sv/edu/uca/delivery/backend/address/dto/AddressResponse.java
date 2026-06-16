package sv.edu.uca.delivery.backend.address.dto;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String label,
        String streetAddress,
        String city,
        String state,
        String country,
        String postalCode,
        Double latitude,
        Double longitude,
        boolean defaultAddress
) {
}
