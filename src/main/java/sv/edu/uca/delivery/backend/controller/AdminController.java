package sv.edu.uca.delivery.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.dto.CommissionRequest;
import sv.edu.uca.delivery.backend.dto.CommissionResponse;
import sv.edu.uca.delivery.backend.dto.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.service.AdminService;
import sv.edu.uca.delivery.backend.service.RestaurantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Configuracion administrativa protegida para ADMIN.")
public class AdminController {

    private final AdminService adminService;
    private final RestaurantService restaurantService;

    @PostMapping("/commissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear configuracion de comision")
    public CommissionResponse createCommission(@RequestBody @Valid CommissionRequest request) {
        return adminService.createCommission(request);
    }

    @GetMapping("/commissions")
    @Operation(summary = "Listar comisiones configuradas")
    public List<CommissionResponse> listCommissions() {
        return adminService.listCommissions();
    }

    @GetMapping("/restaurants")
    @Operation(summary = "Listar restaurantes para admin", description = "Incluye restaurantes activos e inactivos.")
    public List<RestaurantResponseDTO> listRestaurants(@RequestParam(name = "q", required = false) String query) {
        return restaurantService.searchForAdmin(query);
    }

    @PatchMapping("/restaurants/{id}/activate")
    @Operation(summary = "Reactivar restaurante", description = "Permite al administrador volver a activar un restaurante desactivado.")
    public RestaurantResponseDTO activateRestaurant(@PathVariable UUID id) {
        return restaurantService.activate(id);
    }
}
