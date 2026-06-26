package sv.edu.uca.delivery.backend.controller;

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
import sv.edu.uca.delivery.backend.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.dto.DeliveryProfileResponse;
import sv.edu.uca.delivery.backend.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.dto.DeliveryStatsResponse;
import sv.edu.uca.delivery.backend.dto.UpdateDeliveryAvailabilityRequest;
import sv.edu.uca.delivery.backend.dto.UpdateDeliveryLocationRequest;
import sv.edu.uca.delivery.backend.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.service.DeliveryService;

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

    @GetMapping("/requests")
    @Operation(summary = "Solicitudes pendientes", description = "Repartidor autenticado consulta pedidos ofrecidos que puede aceptar o rechazar.")
    public ResponseEntity<List<DeliveryResponse>> getMyRequests() {
        return ResponseEntity.ok(deliveryService.getMyRequests());
    }

    @GetMapping("/active")
    @Operation(summary = "Entregas activas", description = "Entregas aceptadas/en curso del repartidor autenticado.")
    public ResponseEntity<List<DeliveryResponse>> getMyActiveDeliveries() {
        return ResponseEntity.ok(deliveryService.getMyActiveDeliveries());
    }

    @GetMapping("/history")
    @Operation(summary = "Historial de entregas", description = "Entregas completadas, canceladas o rechazadas del repartidor autenticado.")
    public ResponseEntity<List<DeliveryResponse>> getMyHistory() {
        return ResponseEntity.ok(deliveryService.getMyHistory());
    }

    @PatchMapping("/{id}/accept")
    @Operation(summary = "Aceptar solicitud de delivery")
    public ResponseEntity<DeliveryResponse> acceptRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.acceptRequest(id));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Rechazar solicitud de delivery", description = "Registra rechazo y reofrece al siguiente repartidor cercano si existe.")
    public ResponseEntity<DeliveryResponse> rejectRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryService.rejectRequest(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de delivery", description = "Repartidor asignado avanza ASSIGNED -> PICKED_UP -> ON_THE_WAY -> DELIVERED.")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request
    ) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, request));
    }

    @GetMapping("/profile")
    @Operation(summary = "Perfil de repartidor", description = "Disponibilidad y ultima ubicacion registrada.")
    public ResponseEntity<DeliveryProfileResponse> getProfile() {
        return ResponseEntity.ok(deliveryService.getMyProfile());
    }

    @PatchMapping("/location")
    @Operation(summary = "Actualizar ubicacion", description = "Registra coordenadas actuales del repartidor usando PostGIS.")
    public ResponseEntity<DeliveryProfileResponse> updateLocation(@Valid @RequestBody UpdateDeliveryLocationRequest request) {
        return ResponseEntity.ok(deliveryService.updateLocation(request));
    }

    @PatchMapping("/availability")
    @Operation(summary = "Actualizar disponibilidad")
    public ResponseEntity<DeliveryProfileResponse> updateAvailability(@Valid @RequestBody UpdateDeliveryAvailabilityRequest request) {
        return ResponseEntity.ok(deliveryService.updateAvailability(request));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estadisticas del repartidor")
    public ResponseEntity<DeliveryStatsResponse> getStats() {
        return ResponseEntity.ok(deliveryService.getMyStats());
    }
}
