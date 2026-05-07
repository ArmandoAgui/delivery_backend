package sv.edu.uca.delivery.backend.delivery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.delivery.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryAssignmentResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryLocationRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryLocationResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryOrderResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryTrackingResponse;
import sv.edu.uca.delivery.backend.delivery.dto.NearbyDeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.delivery.service.DeliveryService;

@Validated
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/assign")
    public ResponseEntity<DeliveryAssignmentResponse> assignDelivery(@Valid @RequestBody AssignDeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.assignDelivery(request));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<DeliveryOrderResponse>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(deliveryService.getMyOrders(authentication));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryAssignmentResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, request, authentication));
    }

    @PostMapping("/location")
    public ResponseEntity<DeliveryLocationResponse> registerLocation(
            @Valid @RequestBody DeliveryLocationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(deliveryService.registerLocation(request, authentication));
    }

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<DeliveryTrackingResponse> getTracking(@PathVariable UUID orderId) {
        return ResponseEntity.ok(deliveryService.getTracking(orderId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<NearbyDeliveryResponse> findNearestDelivery(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @RequestParam(required = false) @Min(1) Integer radiusMeters
    ) {
        return ResponseEntity.ok(deliveryService.findNearestDelivery(latitude, longitude, radiusMeters));
    }
}
