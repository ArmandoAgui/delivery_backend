package sv.edu.uca.delivery.backend.delivery.mapper;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryAssignmentResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryLocationResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryOrderResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryTrackingResponse;
import sv.edu.uca.delivery.backend.delivery.dto.NearbyDeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryLocationProjection;
import sv.edu.uca.delivery.backend.delivery.repository.NearbyDeliveryProjection;
import sv.edu.uca.delivery.backend.user.entity.Address;

@Component
public class DeliveryMapper {

    public DeliveryAssignmentResponse toAssignmentResponse(DeliveryAssignment assignment) {
        return DeliveryAssignmentResponse.builder()
                .id(assignment.getId())
                .orderId(assignment.getOrder().getId())
                .deliveryUserId(assignment.getDeliveryUser().getId())
                .deliveryFullName(fullName(assignment))
                .status(assignment.getStatus())
                .assignedAt(assignment.getAssignedAt())
                .pickedUpAt(assignment.getPickedUpAt())
                .deliveredAt(assignment.getDeliveredAt())
                .build();
    }

    public DeliveryOrderResponse toDeliveryOrderResponse(DeliveryAssignment assignment) {
        return DeliveryOrderResponse.builder()
                .assignmentId(assignment.getId())
                .orderId(assignment.getOrder().getId())
                .orderStatus(assignment.getOrder().getStatus())
                .deliveryStatus(assignment.getStatus())
                .totalAmount(assignment.getOrder().getTotalAmount())
                .deliveryAddress(formatAddress(assignment.getOrder().getDeliveryAddress()))
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    public DeliveryLocationResponse toLocationResponse(
            UUID deliveryUserId,
            UUID orderId,
            UUID deliveryBatchId,
            DeliveryLocationProjection location
    ) {
        return DeliveryLocationResponse.builder()
                .deliveryUserId(deliveryUserId)
                .orderId(orderId)
                .deliveryBatchId(deliveryBatchId)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .recordedAt(location.getRecordedAt())
                .build();
    }

    public DeliveryTrackingResponse toTrackingResponse(
            DeliveryAssignment assignment,
            Optional<DeliveryLocationProjection> location
    ) {
        return DeliveryTrackingResponse.builder()
                .orderId(assignment.getOrder().getId())
                .assignmentId(assignment.getId())
                .deliveryUserId(assignment.getDeliveryUser().getId())
                .deliveryFullName(fullName(assignment))
                .orderStatus(assignment.getOrder().getStatus())
                .deliveryStatus(assignment.getStatus())
                .lastLatitude(location.map(DeliveryLocationProjection::getLatitude).orElse(null))
                .lastLongitude(location.map(DeliveryLocationProjection::getLongitude).orElse(null))
                .distanceToDestinationMeters(location.map(DeliveryLocationProjection::getDistanceToDestinationMeters).orElse(null))
                .lastLocationRecordedAt(location.map(DeliveryLocationProjection::getRecordedAt).orElse(null))
                .build();
    }

    public NearbyDeliveryResponse toNearbyDeliveryResponse(NearbyDeliveryProjection projection) {
        return NearbyDeliveryResponse.builder()
                .deliveryUserId(projection.getDeliveryUserId())
                .fullName(projection.getFullName())
                .email(projection.getEmail())
                .distanceMeters(projection.getDistanceMeters())
                .build();
    }

    private String fullName(DeliveryAssignment assignment) {
        return assignment.getDeliveryUser().getFirstName() + " " + assignment.getDeliveryUser().getLastName();
    }

    private String formatAddress(Address address) {
        String state = address.getState() == null || address.getState().isBlank() ? "" : ", " + address.getState();
        return address.getStreetAddress() + ", " + address.getCity() + state + ", " + address.getCountry();
    }
}
