package sv.edu.uca.delivery.backend.address.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.address.dto.request.CreateAddressRequest;
import sv.edu.uca.delivery.backend.address.dto.response.AddressResponse;
import sv.edu.uca.delivery.backend.address.entity.Address;
import sv.edu.uca.delivery.backend.address.mapper.AddressMapper;
import sv.edu.uca.delivery.backend.address.repository.AddressRepository;
import sv.edu.uca.delivery.backend.address.service.AddressService;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.exception.UserNotFoundException;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio
 * relacionada con direcciones de usuarios.
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    // Acceso a persistencia de direcciones
    private final AddressRepository addressRepository;

    // Acceso a información de usuarios
    private final UserRepository userRepository;

    // Conversión entre entidades y DTOs
    private final AddressMapper addressMapper;

    /**
     * Crea una nueva dirección asociada
     * a un usuario existente.
     */
    @Override
    public AddressResponse create(UUID userId, CreateAddressRequest request) {

        // Verifica que el usuario exista
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // Construcción de la nueva dirección
        Address address = new Address();
        address.setUser(user);
        address.setLabel(request.getLabel());
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setDefaultAddress(request.isDefaultAddress());

        // Guarda la dirección en base de datos
        Address saved = addressRepository.save(address);

        // Convierte la entidad a DTO de respuesta
        return addressMapper.toResponse(saved);
    }

    /**
     * Obtiene todas las direcciones registradas
     * para un usuario específico.
     */
    @Override
    public List<AddressResponse> findByUser(UUID userId) {

        return addressRepository.findByUserId(userId)
                .stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    /**
     * Elimina una dirección mediante su identificador.
     */
    @Override
    public void delete(UUID addressId) {

        addressRepository.deleteById(addressId);
    }
}
