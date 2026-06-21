package sv.edu.uca.delivery.backend.address.service;

import sv.edu.uca.delivery.backend.address.dto.request.CreateAddressRequest;
import sv.edu.uca.delivery.backend.address.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

/**
 * Define las operaciones disponibles para
 * la administración de direcciones.
 */
public interface AddressService {

    AddressResponse create(UUID userId, CreateAddressRequest request);

    List<AddressResponse> findByUser(UUID userId);

    void delete(UUID addressId);
}
