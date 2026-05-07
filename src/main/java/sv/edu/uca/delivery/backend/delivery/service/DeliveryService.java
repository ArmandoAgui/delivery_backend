package sv.edu.uca.delivery.backend.delivery.service;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import sv.edu.uca.delivery.backend.delivery.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryAssignmentResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryLocationRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryLocationResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryOrderResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryTrackingResponse;
import sv.edu.uca.delivery.backend.delivery.dto.NearbyDeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.dto.UpdateDeliveryStatusRequest;

public interface DeliveryService {

    DeliveryAssignmentResponse assignDelivery(AssignDeliveryRequest request);

    List<DeliveryOrderResponse> getMyOrders(Authentication authentication);

    DeliveryAssignmentResponse updateStatus(UUID assignmentId, UpdateDeliveryStatusRequest request, Authentication authentication);

    DeliveryLocationResponse registerLocation(DeliveryLocationRequest request, Authentication authentication);

    DeliveryTrackingResponse getTracking(UUID orderId);

    NearbyDeliveryResponse findNearestDelivery(Double latitude, Double longitude, Integer radiusMeters);
}
