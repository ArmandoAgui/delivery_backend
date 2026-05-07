package sv.edu.uca.delivery.backend.delivery.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NearbyDeliveryResponse {

    private UUID deliveryUserId;
    private String fullName;
    private String email;
    private Double distanceMeters;
}
