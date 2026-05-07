package sv.edu.uca.delivery.backend.delivery.dto;

import jakarta.validation.constraints.NotNull;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDeliveryStatusRequest {

    @NotNull
    private DeliveryStatus status;
}
