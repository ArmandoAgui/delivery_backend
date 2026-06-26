package sv.edu.uca.delivery.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.uca.delivery.backend.entity.RoleName;
import sv.edu.uca.delivery.backend.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.exception.DeliveryBusinessException;
import sv.edu.uca.delivery.backend.mapper.DeliveryMapper;
import sv.edu.uca.delivery.backend.repository.DeliveryAssignmentRepository;
import sv.edu.uca.delivery.backend.entity.Order;
import sv.edu.uca.delivery.backend.entity.OrderStatus;
import sv.edu.uca.delivery.backend.repository.OrderRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.entity.User;
import sv.edu.uca.delivery.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(
                orderRepository,
                userRepository,
                deliveryAssignmentRepository,
                new DeliveryMapper(),
                authenticatedUserProvider
        );
    }

    @Test
    void assignAutomaticallyCreatesOfferedRequestWithNearestAvailableDeliveryUser() {
        User deliveryUser = deliveryUser();
        Order order = order(OrderStatus.READY_FOR_PICKUP);

        when(deliveryAssignmentRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(deliveryAssignmentRepository.findNearestCandidateForOrder(order.getId()))
                .thenReturn(Optional.of(deliveryUser));
        when(deliveryAssignmentRepository.save(any(DeliveryAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.assignAutomatically(order);

        ArgumentCaptor<DeliveryAssignment> assignmentCaptor = ArgumentCaptor.forClass(DeliveryAssignment.class);
        verify(deliveryAssignmentRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getOrder()).isSameAs(order);
        assertThat(assignmentCaptor.getValue().getDeliveryUser()).isSameAs(deliveryUser);
        assertThat(response.status()).isEqualTo(DeliveryStatus.OFFERED);
        assertThat(response.orderId()).isEqualTo(order.getId());
    }

    @Test
    void assignAutomaticallyRejectsCancelledOrder() {
        Order order = order(OrderStatus.CANCELLED);

        assertThatThrownBy(() -> deliveryService.assignAutomatically(order))
                .isInstanceOf(DeliveryBusinessException.class)
                .hasMessage("Cancelled orders cannot be assigned");

        verify(deliveryAssignmentRepository, never()).save(any());
    }

    @Test
    void assignAutomaticallyRejectsOrderThatAlreadyHasAssignment() {
        Order order = order(OrderStatus.READY_FOR_PICKUP);

        when(deliveryAssignmentRepository.existsByOrderId(order.getId())).thenReturn(true);

        assertThatThrownBy(() -> deliveryService.assignAutomatically(order))
                .isInstanceOf(DeliveryBusinessException.class)
                .hasMessage("Order already has a delivery assignment");

        verify(deliveryAssignmentRepository, never()).save(any());
    }

    @Test
    void assignDeliveryEndpointIsDisabled() {
        UUID orderId = UUID.randomUUID();

        assertThatThrownBy(() -> deliveryService.assignDelivery(new AssignDeliveryRequest(orderId)))
                .isInstanceOf(sv.edu.uca.delivery.backend.exception.BusinessException.class)
                .hasMessage("Manual delivery assignment is disabled; confirm the order to assign automatically");

        verify(orderRepository, never()).findWithLockingById(any());
        verify(deliveryAssignmentRepository, never()).save(any());
    }

    @Test
    void updateStatusRejectsInvalidTransition() {
        User deliveryUser = deliveryUser();
        DeliveryAssignment assignment = assignment(deliveryUser, DeliveryStatus.ASSIGNED, OrderStatus.READY_FOR_PICKUP);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(deliveryUser.getId());
        when(userRepository.findActiveUserByIdAndRole(deliveryUser.getId(), RoleName.DELIVERY))
                .thenReturn(Optional.of(deliveryUser));
        when(deliveryAssignmentRepository.findWithLockingById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> deliveryService.updateStatus(
                assignment.getId(),
                new UpdateDeliveryStatusRequest(DeliveryStatus.DELIVERED)
        ))
                .isInstanceOf(DeliveryBusinessException.class)
                .hasMessage("Invalid delivery status transition");
    }

    @Test
    void updateStatusRejectsDeliveredAssignment() {
        User deliveryUser = deliveryUser();
        DeliveryAssignment assignment = assignment(deliveryUser, DeliveryStatus.DELIVERED, OrderStatus.DELIVERED);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(deliveryUser.getId());
        when(userRepository.findActiveUserByIdAndRole(deliveryUser.getId(), RoleName.DELIVERY))
                .thenReturn(Optional.of(deliveryUser));
        when(deliveryAssignmentRepository.findWithLockingById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> deliveryService.updateStatus(
                assignment.getId(),
                new UpdateDeliveryStatusRequest(DeliveryStatus.PICKED_UP)
        ))
                .isInstanceOf(DeliveryBusinessException.class)
                .hasMessage("Delivered assignments cannot be updated");
    }

    @Test
    void updateStatusToDeliveredMarksOrderDelivered() {
        User deliveryUser = deliveryUser();
        DeliveryAssignment assignment = assignment(deliveryUser, DeliveryStatus.ON_THE_WAY, OrderStatus.ON_THE_WAY);
        assignment.getOrder().setDeliveryFee(new BigDecimal("3.00"));
        assignment.getOrder().setTipAmount(new BigDecimal("2.00"));
        assignment.getOrder().setDiscountAmount(new BigDecimal("10.00"));
        assignment.getOrder().setTotalAmount(new BigDecimal("20.00"));

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(deliveryUser.getId());
        when(userRepository.findActiveUserByIdAndRole(deliveryUser.getId(), RoleName.DELIVERY))
                .thenReturn(Optional.of(deliveryUser));
        when(deliveryAssignmentRepository.findWithLockingById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(deliveryAssignmentRepository.save(any(DeliveryAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.updateStatus(
                assignment.getId(),
                new UpdateDeliveryStatusRequest(DeliveryStatus.DELIVERED)
        );

        assertThat(response.status()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(assignment.getOrder().getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(assignment.getDeliveredAt()).isNotNull();
        assertThat(assignment.getDeliveryGrossEarnings()).isEqualByComparingTo("5.00");
        assertThat(assignment.getDeliveryPlatformCommissionAmount()).isEqualByComparingTo("0.00");
        assertThat(assignment.getDeliveryNetEarnings()).isEqualByComparingTo("5.00");
    }

    private User deliveryUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Ada");
        user.setLastName("Lovelace");
        user.setEmail("ada@example.com");
        user.setActive(true);
        return user;
    }

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(status);
        return order;
    }

    private DeliveryAssignment assignment(User deliveryUser, DeliveryStatus deliveryStatus, OrderStatus orderStatus) {
        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setOrder(order(orderStatus));
        assignment.setDeliveryUser(deliveryUser);
        assignment.setStatus(deliveryStatus);
        return assignment;
    }
}
