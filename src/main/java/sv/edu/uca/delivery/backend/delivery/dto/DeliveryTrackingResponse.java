package sv.edu.uca.delivery.backend.delivery.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;

@Getter
@Builder
public class DeliveryTrackingResponse {

    private UUID orderId;
    private UUID assignmentId;
    private UUID deliveryUserId;
    private String deliveryFullName;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;
    private Double lastLatitude;
    private Double lastLongitude;
    private Double distanceToDestinationMeters;
    private LocalDateTime lastLocationRecordedAt;
}
