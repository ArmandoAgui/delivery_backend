package sv.edu.uca.delivery.backend.address.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.address.dto.AddressRequest;
import sv.edu.uca.delivery.backend.address.dto.AddressResponse;
import sv.edu.uca.delivery.backend.address.service.AddressService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Direcciones del cliente autenticado.")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Listar mis direcciones")
    public List<AddressResponse> myAddresses() {
        return addressService.myAddresses();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear direccion")
    public AddressResponse create(@RequestBody @Valid AddressRequest request) {
        return addressService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar direccion propia")
    public AddressResponse update(@PathVariable UUID id, @RequestBody @Valid AddressRequest request) {
        return addressService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar direccion propia")
    public void delete(@PathVariable UUID id) {
        addressService.delete(id);
    }
}
