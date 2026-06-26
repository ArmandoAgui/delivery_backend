package sv.edu.uca.delivery.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.dto.LoyaltyResponse;
import sv.edu.uca.delivery.backend.dto.RedeemPointsRequest;
import sv.edu.uca.delivery.backend.service.LoyaltyService;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty", description = "Consulta y canje de puntos de fidelidad del cliente autenticado.")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping
    @Operation(summary = "Consultar puntos propios", description = "Obtiene el balance de fidelidad del cliente autenticado.")
    public LoyaltyResponse mine() {
        return loyaltyService.mine();
    }

    @PostMapping("/redeem")
    @Operation(summary = "Canjear puntos", description = "Canjea puntos disponibles del cliente autenticado.")
    public LoyaltyResponse redeem(@RequestBody @Valid RedeemPointsRequest request) {
        return loyaltyService.redeem(request);
    }
}
