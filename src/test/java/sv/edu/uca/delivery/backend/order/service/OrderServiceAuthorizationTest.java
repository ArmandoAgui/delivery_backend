package sv.edu.uca.delivery.backend.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.uca.delivery.backend.address.entity.Address;
import sv.edu.uca.delivery.backend.address.repository.AddressRepository;
import sv.edu.uca.delivery.backend.auth.entity.Role;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.cart.repository.CartRepository;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.coupon.repository.CouponRedemptionRepository;
import sv.edu.uca.delivery.backend.coupon.repository.CouponRepository;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryAssignmentRepository;
import sv.edu.uca.delivery.backend.delivery.service.DeliveryEstimateService;
import sv.edu.uca.delivery.backend.delivery.service.DeliveryService;
import sv.edu.uca.delivery.backend.loyalty.service.LoyaltyService;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.order.repository.OrderStatusHistoryRepository;
import sv.edu.uca.delivery.backend.payment.repository.PaymentRepository;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceAuthorizationTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusHistoryRepository historyRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponRedemptionRepository couponRedemptionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    @Mock
    private DeliveryEstimateService deliveryEstimateService;

    @Mock
    private ObjectProvider<DeliveryService> deliveryServiceProvider;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private OrderFactory orderFactory;

    @Mock
    private LoyaltyService loyaltyService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                historyRepository,
                cartRepository,
                addressRepository,
                userRepository,
                couponRepository,
                couponRedemptionRepository,
                paymentRepository,
                deliveryAssignmentRepository,
                deliveryEstimateService,
                deliveryServiceProvider,
                authenticatedUserProvider,
                orderFactory,
                loyaltyService
        );
    }

    @Test
    void cancelRejectsUserThatDoesNotOwnOrder() {
        User customer = user(RoleName.CUSTOMER);
        User otherCustomer = user(RoleName.CUSTOMER);
        Order order = order(customer, user(RoleName.RESTAURANT), OrderStatus.CREATED);

        when(orderRepository.findWithLockingById(order.getId())).thenReturn(Optional.of(order));
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(otherCustomer.getId());
        when(userRepository.findByIdWithRole(otherCustomer.getId())).thenReturn(Optional.of(otherCustomer));

        assertThatThrownBy(() -> orderService.cancel(order.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only the order owner or admin can cancel this order");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmRejectsRestaurantThatDoesNotOwnOrder() {
        User restaurantOwner = user(RoleName.RESTAURANT);
        User otherRestaurantOwner = user(RoleName.RESTAURANT);
        Order order = order(user(RoleName.CUSTOMER), restaurantOwner, OrderStatus.CREATED);

        when(orderRepository.findWithLockingById(order.getId())).thenReturn(Optional.of(order));
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(otherRestaurantOwner.getId());
        when(userRepository.findByIdWithRole(otherRestaurantOwner.getId())).thenReturn(Optional.of(otherRestaurantOwner));

        assertThatThrownBy(() -> orderService.confirm(order.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only the restaurant owner or admin can update this order");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getRejectsDeliveryUserThatIsNotAssignedToOrder() {
        User deliveryUser = user(RoleName.DELIVERY);
        Order order = order(user(RoleName.CUSTOMER), user(RoleName.RESTAURANT), OrderStatus.CONFIRMED);

        when(orderRepository.findDetailById(order.getId())).thenReturn(Optional.of(order));
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(deliveryUser.getId());
        when(userRepository.findByIdWithRole(deliveryUser.getId())).thenReturn(Optional.of(deliveryUser));
        when(deliveryAssignmentRepository.findByOrderId(order.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.get(order.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order is not visible for current user");
    }

    private Order order(User customer, User restaurantOwner, OrderStatus status) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        restaurant.setOwner(restaurantOwner);

        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setUser(customer);
        address.setStreetAddress("Calle Principal 123");
        address.setCity("San Salvador");

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(address);
        order.setStatus(status);
        return order;
    }

    private User user(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName(roleName.name());
        user.setLastName("User");
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}
