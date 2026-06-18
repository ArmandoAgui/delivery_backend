package sv.edu.uca.delivery.backend.delivery.mapper;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.user.entity.User;

@Component
public class DeliveryMapper {

    public DeliveryResponse toResponse(DeliveryAssignment assignment) {
        User deliveryUser = assignment.getDeliveryUser();
        Order order = assignment.getOrder();
        String address = order.getDeliveryAddress() == null
                ? null
                : order.getDeliveryAddress().getStreetAddress() + ", "
                + order.getDeliveryAddress().getCity() + ", "
                + order.getDeliveryAddress().getCountry();
        String restaurantAddress = order.getRestaurant() == null
                ? null
                : order.getRestaurant().getStreetAddress() + ", "
                + order.getRestaurant().getCity() + ", "
                + order.getRestaurant().getCountry();
        String summary = order.getItems() == null || order.getItems().isEmpty()
                ? "Order " + order.getId()
                : order.getItems().stream()
                .map(item -> item.getQuantity() + "x " + item.getProductName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("Order " + order.getId());
        return new DeliveryResponse(
                assignment.getId(),
                order.getId(),
                deliveryUser.getId(),
                deliveryUser.getFirstName() + " " + deliveryUser.getLastName(),
                assignment.getStatus(),
                order.getStatus(),
                order.getRestaurant() == null ? null : order.getRestaurant().getName(),
                restaurantAddress,
                address,
                summary,
                order.getDistanceKm(),
                order.getDeliveryFee(),
                order.getTipAmount(),
                order.getTotalAmount(),
                assignment.getAssignedAt(),
                assignment.getPickedUpAt(),
                assignment.getDeliveredAt(),
                assignment.getCreatedAt()
        );
    }
}
