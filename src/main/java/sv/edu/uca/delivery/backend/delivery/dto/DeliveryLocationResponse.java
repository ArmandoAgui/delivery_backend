package sv.edu.uca.delivery.backend.delivery.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryLocationResponse {

    private UUID deliveryUserId;
    private UUID orderId;
    private UUID deliveryBatchId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime recordedAt;
}
