package sv.edu.uca.delivery.backend.address.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.address.dto.request.CreateAddressRequest;
import sv.edu.uca.delivery.backend.address.dto.response.AddressResponse;
import sv.edu.uca.delivery.backend.address.service.AddressService;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST encargado de gestionar
 * las operaciones relacionadas con direcciones.
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    // Servicio encargado de la lógica de negocio
    private final AddressService addressService;

    /**
     * Registra una nueva dirección para el usuario.
     */
    @PostMapping("/user/{userId}")
    public AddressResponse create(
            @PathVariable UUID userId,
            @RequestBody @Valid CreateAddressRequest request
    ) {
        return addressService.create(userId, request);
    }

    /**
     * Obtiene todas las direcciones asociadas
     * al usuario autenticado.
     */
    @GetMapping("/user/{userId}")
    public List<AddressResponse> findByUser(
            @PathVariable UUID userId
    ) {
        return addressService.findByUser(userId);
    }

    /**
     * Elimina una dirección existente.
     */
    @DeleteMapping("/{addressId}")
    public void delete(
            @PathVariable UUID addressId
    ) {
        addressService.delete(addressId);
    }
}
