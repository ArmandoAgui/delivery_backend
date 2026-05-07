package sv.edu.uca.delivery.backend.delivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;

@Getter
@Builder
public class DeliveryOrderResponse {

    private UUID assignmentId;
    private UUID orderId;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private LocalDateTime assignedAt;
}
