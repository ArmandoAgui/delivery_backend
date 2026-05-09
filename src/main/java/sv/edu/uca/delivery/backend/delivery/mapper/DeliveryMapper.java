package sv.edu.uca.delivery.backend.delivery.mapper;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.user.entity.User;

@Component
public class DeliveryMapper {

    public DeliveryResponse toResponse(DeliveryAssignment assignment) {
        User deliveryUser = assignment.getDeliveryUser();
        return new DeliveryResponse(
                assignment.getId(),
                assignment.getOrder().getId(),
                deliveryUser.getId(),
                deliveryUser.getFirstName() + " " + deliveryUser.getLastName(),
                assignment.getStatus(),
                assignment.getOrder().getStatus(),
                assignment.getAssignedAt(),
                assignment.getPickedUpAt(),
                assignment.getDeliveredAt(),
                assignment.getCreatedAt()
        );
    }
}
