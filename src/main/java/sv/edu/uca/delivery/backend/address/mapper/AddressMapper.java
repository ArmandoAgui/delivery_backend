package sv.edu.uca.delivery.backend.address.mapper;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.address.dto.response.AddressResponse;
import sv.edu.uca.delivery.backend.address.entity.Address;

@Component
public class AddressMapper {

    public AddressResponse toResponse(
            Address address
    ) {

        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .streetAddress(address.getStreetAddress())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}