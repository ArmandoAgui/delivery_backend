package sv.edu.uca.delivery.backend.delivery.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;

@Getter
@Builder
public class DeliveryAssignmentResponse {

    private UUID id;
    private UUID orderId;
    private UUID deliveryUserId;
    private String deliveryFullName;
    private DeliveryStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
}
