package sv.edu.uca.delivery.backend.address.dto.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.address.dto.request.CreateAddressRequest;
import sv.edu.uca.delivery.backend.address.dto.response.AddressResponse;
import sv.edu.uca.delivery.backend.address.service.AddressService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/user/{userId}")
    public AddressResponse create(
            @PathVariable UUID userId,
            @RequestBody @Valid CreateAddressRequest request
    ) {
        return addressService.create(userId, request);
    }

    @GetMapping("/user/{userId}")
    public List<AddressResponse> findByUser(
            @PathVariable UUID userId
    ) {
        return addressService.findByUser(userId);
    }

    @DeleteMapping("/{addressId}")
    public void delete(
            @PathVariable UUID addressId
    ) {
        addressService.delete(addressId);
    }
}
