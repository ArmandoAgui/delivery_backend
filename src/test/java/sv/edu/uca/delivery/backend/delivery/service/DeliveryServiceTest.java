package sv.edu.uca.delivery.backend.delivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.delivery.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.delivery.exception.DeliveryBusinessException;
import sv.edu.uca.delivery.backend.delivery.mapper.DeliveryMapper;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryAssignmentRepository;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

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
    void assignDeliveryCreatesAssignmentWithNearestAvailableDeliveryUser() {
        UUID orderId = UUID.randomUUID();
        User admin = adminUser();
        User deliveryUser = deliveryUser();
        Order order = order(OrderStatus.READY_FOR_PICKUP);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(admin.getId());
        when(userRepository.findByIdAndActiveTrueAndRoleName(admin.getId(), RoleName.ADMIN))
                .thenReturn(Optional.of(admin));
        when(orderRepository.findWithLockingById(orderId)).thenReturn(Optional.of(order));
        when(deliveryAssignmentRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(deliveryAssignmentRepository.findNearestAvailableDeliveryUser(order.getId()))
                .thenReturn(Optional.of(deliveryUser));
        when(userRepository.findActiveUserByIdAndRole(deliveryUser.getId(), RoleName.DELIVERY))
                .thenReturn(Optional.of(deliveryUser));
        when(deliveryAssignmentRepository.existsByDeliveryUserIdAndStatusIn(eq(deliveryUser.getId()), any()))
                .thenReturn(false);
        when(deliveryAssignmentRepository.save(any(DeliveryAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.assignDelivery(new AssignDeliveryRequest(orderId));

        ArgumentCaptor<DeliveryAssignment> assignmentCaptor = ArgumentCaptor.forClass(DeliveryAssignment.class);
        verify(deliveryAssignmentRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getOrder()).isSameAs(order);
        assertThat(assignmentCaptor.getValue().getDeliveryUser()).isSameAs(deliveryUser);
        assertThat(response.status()).isEqualTo(DeliveryStatus.ASSIGNED);
        assertThat(response.orderId()).isEqualTo(order.getId());
    }

    @Test
    void assignDeliveryRejectsCancelledOrder() {
        UUID orderId = UUID.randomUUID();
        User admin = adminUser();

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(admin.getId());
        when(userRepository.findByIdAndActiveTrueAndRoleName(admin.getId(), RoleName.ADMIN))
                .thenReturn(Optional.of(admin));
        when(orderRepository.findWithLockingById(orderId)).thenReturn(Optional.of(order(OrderStatus.CANCELLED)));

        assertThatThrownBy(() -> deliveryService.assignDelivery(new AssignDeliveryRequest(orderId)))
                .isInstanceOf(DeliveryBusinessException.class)
                .hasMessage("Cancelled orders cannot be assigned");

        verify(deliveryAssignmentRepository, never()).save(any());
    }

    @Test
    void assignDeliveryRejectsOrderThatAlreadyHasAssignment() {
        UUID orderId = UUID.randomUUID();
        User admin = adminUser();
        Order order = order(OrderStatus.READY_FOR_PICKUP);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(admin.getId());
        when(userRepository.findByIdAndActiveTrueAndRoleName(admin.getId(), RoleName.ADMIN))
                .thenReturn(Optional.of(admin));
        when(orderRepository.findWithLockingById(orderId)).thenReturn(Optional.of(order));
        when(deliveryAssignmentRepository.existsByOrderId(order.getId())).thenReturn(true);

        assertThatThrownBy(() -> deliveryService.assignDelivery(new AssignDeliveryRequest(orderId)))
                .isInstanceOf(DeliveryBusinessException.class)
                .hasMessage("Order already has a delivery assignment");

        verify(deliveryAssignmentRepository, never()).save(any());
    }

    @Test
    void assignDeliveryRejectsNonAdminCaller() {
        UUID orderId = UUID.randomUUID();
        User deliveryUser = deliveryUser();

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(deliveryUser.getId());
        when(userRepository.findByIdAndActiveTrueAndRoleName(deliveryUser.getId(), RoleName.ADMIN))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.assignDelivery(new AssignDeliveryRequest(orderId)))
                .isInstanceOf(sv.edu.uca.delivery.backend.common.exception.BusinessException.class)
                .hasMessage("Only admins can assign delivery");

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

    private User adminUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Grace");
        user.setLastName("Admin");
        user.setEmail("grace@example.com");
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
