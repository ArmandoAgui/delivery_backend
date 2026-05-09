package sv.edu.uca.delivery.backend.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.delivery.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.delivery.exception.DeliveryBusinessException;
import sv.edu.uca.delivery.backend.delivery.exception.DeliveryNotFoundException;
import sv.edu.uca.delivery.backend.delivery.mapper.DeliveryMapper;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryAssignmentRepository;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final EnumSet<DeliveryStatus> BUSY_STATUSES = EnumSet.of(
            DeliveryStatus.ASSIGNED,
            DeliveryStatus.PICKED_UP,
            DeliveryStatus.ON_THE_WAY
    );

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryMapper deliveryMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public DeliveryResponse assignDelivery(AssignDeliveryRequest request) {
        Order order = orderRepository.findWithLockingById(request.orderId())
                .orElseThrow(() -> new DeliveryNotFoundException("Order was not found"));

        validateOrderCanBeAssigned(order);
        if (deliveryAssignmentRepository.existsByOrderId(order.getId())) {
            throw new DeliveryBusinessException("Order already has a delivery assignment");
        }

        User deliveryUser = deliveryAssignmentRepository.findNearestAvailableDeliveryUser(order.getId())
                .orElseThrow(() -> new DeliveryBusinessException("No available delivery user was found near the order"));

        validateDeliveryUser(deliveryUser.getId());
        if (deliveryAssignmentRepository.existsByDeliveryUserIdAndStatusIn(deliveryUser.getId(), BUSY_STATUSES)) {
            throw new DeliveryBusinessException("Delivery user is already assigned to an active order");
        }

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setOrder(order);
        assignment.setDeliveryUser(deliveryUser);
        assignment.setStatus(DeliveryStatus.ASSIGNED);

        return deliveryMapper.toResponse(deliveryAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getMyOrders() {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);

        return deliveryAssignmentRepository.findAllByDeliveryUserIdOrderByAssignedAtDesc(deliveryUserId)
                .stream()
                .map(deliveryMapper::toResponse)
                .toList();
    }

    @Transactional
    public DeliveryResponse updateStatus(UUID deliveryAssignmentId, UpdateDeliveryStatusRequest request) {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);

        DeliveryAssignment assignment = deliveryAssignmentRepository.findWithLockingById(deliveryAssignmentId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery assignment was not found"));

        if (!assignment.getDeliveryUser().getId().equals(deliveryUserId)) {
            throw new DeliveryBusinessException("Delivery assignment does not belong to the authenticated delivery user");
        }

        validateStatusChange(assignment, request.status());
        applyStatusChange(assignment, request.status());

        return deliveryMapper.toResponse(deliveryAssignmentRepository.save(assignment));
    }

    private void validateOrderCanBeAssigned(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new DeliveryBusinessException("Cancelled orders cannot be assigned");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new DeliveryBusinessException("Delivered orders cannot be assigned");
        }
    }

    private void validateDeliveryUser(UUID deliveryUserId) {
        userRepository.findActiveUserByIdAndRole(deliveryUserId, RoleName.DELIVERY)
                .orElseThrow(() -> new DeliveryBusinessException("Delivery user must exist, be active, and have DELIVERY role"));
    }

    private void validateStatusChange(DeliveryAssignment assignment, DeliveryStatus requestedStatus) {
        if (requestedStatus == DeliveryStatus.CANCELLED) {
            throw new DeliveryBusinessException("Delivery cancellation is not supported by this endpoint");
        }
        if (assignment.getOrder().getStatus() == OrderStatus.CANCELLED) {
            throw new DeliveryBusinessException("Cancelled orders cannot be delivered or updated");
        }
        if (assignment.getStatus() == DeliveryStatus.DELIVERED) {
            throw new DeliveryBusinessException("Delivered assignments cannot be updated");
        }
        if (!isValidTransition(assignment.getStatus(), requestedStatus)) {
            throw new DeliveryBusinessException("Invalid delivery status transition");
        }
    }

    private boolean isValidTransition(DeliveryStatus currentStatus, DeliveryStatus requestedStatus) {
        return (currentStatus == DeliveryStatus.ASSIGNED && requestedStatus == DeliveryStatus.PICKED_UP)
                || (currentStatus == DeliveryStatus.PICKED_UP && requestedStatus == DeliveryStatus.ON_THE_WAY)
                || (currentStatus == DeliveryStatus.ON_THE_WAY && requestedStatus == DeliveryStatus.DELIVERED);
    }

    private void applyStatusChange(DeliveryAssignment assignment, DeliveryStatus requestedStatus) {
        LocalDateTime now = LocalDateTime.now();
        assignment.setStatus(requestedStatus);

        if (requestedStatus == DeliveryStatus.PICKED_UP) {
            assignment.setPickedUpAt(now);
        }
        if (requestedStatus == DeliveryStatus.ON_THE_WAY) {
            assignment.getOrder().setStatus(OrderStatus.ON_THE_WAY);
        }
        if (requestedStatus == DeliveryStatus.DELIVERED) {
            assignment.setDeliveredAt(now);
            assignment.getOrder().setStatus(OrderStatus.DELIVERED);
        }
    }
}
