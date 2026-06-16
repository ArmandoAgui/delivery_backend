package sv.edu.uca.delivery.backend.delivery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.delivery.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.delivery.service.DeliveryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Asignacion de repartidor y actualizacion de estados de entrega.")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/assign")
    @Operation(summary = "Asignar repartidor", description = "Solo ADMIN. Asigna automaticamente un repartidor disponible a un pedido confirmado/listo.")
    public ResponseEntity<DeliveryResponse> assignDelivery(@Valid @RequestBody AssignDeliveryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.assignDelivery(request));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Mis entregas asignadas", description = "Repartidor autenticado consulta sus entregas.")
    public ResponseEntity<List<DeliveryResponse>> getMyOrders() {
        return ResponseEntity.ok(deliveryService.getMyOrders());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de delivery", description = "Repartidor asignado avanza ASSIGNED -> PICKED_UP -> ON_THE_WAY -> DELIVERED.")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request
    ) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, request));
    }
}
