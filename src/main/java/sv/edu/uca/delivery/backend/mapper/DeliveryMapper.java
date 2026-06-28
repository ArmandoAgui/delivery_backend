package sv.edu.uca.delivery.backend.mapper;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.entity.Order;
import sv.edu.uca.delivery.backend.entity.User;

@Component
public class DeliveryMapper {

    private static final String DEFAULT_COUNTRY = "El Salvador";

    public DeliveryResponse toResponse(DeliveryAssignment assignment) {
        User deliveryUser = assignment.getDeliveryUser();
        Order order = assignment.getOrder();
        String address = order.getDeliveryAddress() == null
                ? null
                : order.getDeliveryAddress().getStreetAddress() + ", "
                + order.getDeliveryAddress().getCity() + ", "
                + DEFAULT_COUNTRY;
        String restaurantAddress = order.getRestaurant() == null
                ? null
                : order.getRestaurant().getStreetAddress() + ", "
                + order.getRestaurant().getDepartment() + ", "
                + DEFAULT_COUNTRY;
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
