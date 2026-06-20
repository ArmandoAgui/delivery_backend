package sv.edu.uca.delivery.backend.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.address.entity.Address;
import sv.edu.uca.delivery.backend.address.repository.AddressRepository;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.cart.entity.Cart;
import sv.edu.uca.delivery.backend.cart.entity.CartStatus;
import sv.edu.uca.delivery.backend.cart.repository.CartRepository;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.coupon.entity.Coupon;
import sv.edu.uca.delivery.backend.coupon.entity.CouponRedemption;
import sv.edu.uca.delivery.backend.coupon.repository.CouponRedemptionRepository;
import sv.edu.uca.delivery.backend.coupon.repository.CouponRepository;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryAssignmentRepository;
import sv.edu.uca.delivery.backend.delivery.service.DeliveryEstimateService;
import sv.edu.uca.delivery.backend.delivery.service.DeliveryService;
import sv.edu.uca.delivery.backend.loyalty.service.LoyaltyService;
import sv.edu.uca.delivery.backend.order.dto.request.CreateOrderFromCartRequest;
import sv.edu.uca.delivery.backend.order.dto.response.OrderItemResponse;
import sv.edu.uca.delivery.backend.order.dto.response.OrderResponse;
import sv.edu.uca.delivery.backend.order.dto.response.OrderStatusHistoryResponse;
import sv.edu.uca.delivery.backend.order.dto.response.OrderTrackingResponse;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.order.entity.OrderItem;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;
import sv.edu.uca.delivery.backend.order.entity.OrderStatusHistory;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.order.repository.OrderStatusHistoryRepository;
import sv.edu.uca.delivery.backend.payment.entity.Payment;
import sv.edu.uca.delivery.backend.payment.entity.PaymentStatus;
import sv.edu.uca.delivery.backend.payment.repository.PaymentRepository;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.restaurant.repository.RestaurantScheduleRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.13");
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final PaymentRepository paymentRepository;
    private final RestaurantScheduleRepository restaurantScheduleRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryEstimateService deliveryEstimateService;
    private final ObjectProvider<DeliveryService> deliveryServiceProvider;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final OrderFactory orderFactory;
    private final LoyaltyService loyaltyService;

    @Transactional
    public OrderResponse createFromCart(CreateOrderFromCartRequest request) {
        UUID customerId = authenticatedUserProvider.getCurrentUserId();
        User customer = userRepository.findByIdAndActiveTrueAndRoleName(customerId, RoleName.CUSTOMER)
                .orElseThrow(() -> new BusinessException(HttpStatus.FORBIDDEN, "Only customers can create orders"));
        Cart cart = cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        if (!isRestaurantAcceptingOrders(cart.getRestaurant(), LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Restaurant is currently closed");
        }
        Address address = addressRepository.findByIdAndUserId(request.deliveryAddressId(), customerId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Delivery address not found"));

        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tip = request.tipAmount() == null ? BigDecimal.ZERO : request.tipAmount();
        Coupon coupon = findCoupon(request.couponCode());
        BigDecimal couponDiscount = coupon == null ? BigDecimal.ZERO : calculateDiscount(coupon, subtotal);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        var deliveryEstimate = deliveryEstimateService.estimate(
                cart.getRestaurant(),
                address,
                cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum()
        );

        Order order = orderFactory.fromCart(
                customer,
                cart.getRestaurant(),
                address,
                cart,
                subtotal,
                tax,
                deliveryEstimate,
                tip,
                couponDiscount,
                coupon == null ? null : coupon.getId(),
                request.notes()
        );
        cart.getItems().forEach(cartItem -> {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(cartItem.getProduct().getId());
            item.setProductName(cartItem.getProduct().getName());
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(cartItem.getUnitPrice());
            item.setLineTotal(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.getItems().add(item);
        });

        Order saved = orderRepository.save(order);
        if (Boolean.TRUE.equals(request.useLoyaltyPoints())) {
            BigDecimal loyaltyDiscount = loyaltyService.redeemAllForOrder(customer, saved, saved.getTotalAmount());
            if (loyaltyDiscount.compareTo(BigDecimal.ZERO) > 0) {
                saved.setDiscountAmount(saved.getDiscountAmount().add(loyaltyDiscount));
                saved.setTotalAmount(saved.getTotalAmount().subtract(loyaltyDiscount).max(BigDecimal.ZERO));
                saved = orderRepository.save(saved);
            }
        }
        addHistory(saved, null, OrderStatus.CREATED, customer, "Order created from cart");
        if (coupon != null) {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            CouponRedemption redemption = new CouponRedemption();
            redemption.setCoupon(coupon);
            redemption.setOrder(saved);
            redemption.setCustomer(customer);
            redemption.setDiscountAmount(couponDiscount);
            couponRedemptionRepository.save(redemption);
        }
        Payment payment = new Payment();
        payment.setOrder(saved);
        payment.setAmount(saved.getTotalAmount());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        Order order = orderRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        validateCanView(order);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myHistory() {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(authenticatedUserProvider.getCurrentUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> restaurantOrders() {
        return orderRepository.findByRestaurantOwnerIdOrderByCreatedAtDesc(authenticatedUserProvider.getCurrentUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        Order order = orderRepository.findWithLockingById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        requireCustomerOwnerOrAdmin(order);
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException(HttpStatus.CONFLICT, "Only created orders can be cancelled");
        }
        OrderStatus previous = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        addHistory(order, previous, OrderStatus.CANCELLED, currentUser(), "Order cancelled");
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse confirm(UUID id) {
        Order order = orderRepository.findWithLockingById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        requireRestaurantOwnerOrAdmin(order);
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException(HttpStatus.CONFLICT, "Only created orders can be confirmed");
        }
        OrderStatus previous = order.getStatus();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        addHistory(order, previous, OrderStatus.CONFIRMED, currentUser(), "Restaurant confirmed order");
        Order saved = orderRepository.save(order);
        deliveryServiceProvider.getObject().assignAutomatically(saved);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse reject(UUID id) {
        Order order = orderRepository.findWithLockingById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        requireRestaurantOwnerOrAdmin(order);
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException(HttpStatus.CONFLICT, "Only created orders can be rejected");
        }
        OrderStatus previous = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        addHistory(order, previous, OrderStatus.CANCELLED, currentUser(), "Restaurant rejected order");
        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderTrackingResponse tracking(UUID id) {
        Order order = orderRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        validateCanView(order);
        Address address = order.getDeliveryAddress();
        String textAddress = address.getStreetAddress() + ", " + address.getCity() + ", El Salvador";
        var assignment = deliveryAssignmentRepository.findByOrderId(order.getId()).orElse(null);
        return new OrderTrackingResponse(
                order.getId(),
                order.getStatus(),
                order.getRestaurant().getName(),
                textAddress,
                assignment == null ? null : assignment.getStatus().name(),
                assignment == null ? null : assignment.getDeliveryUser().getId(),
                assignment == null ? null : assignment.getDeliveryUser().getFirstName() + " " + assignment.getDeliveryUser().getLastName(),
                order.getEstimatedDeliveryMinutes(),
                order.getDeliveryFee(),
                order.getDistanceKm(),
                order.getDemandMultiplier() != null && order.getDemandMultiplier().compareTo(BigDecimal.ONE) > 0,
                historyRepository.findByOrderIdOrderByChangedAtAsc(order.getId())
                        .stream()
                        .map(this::toHistory)
                        .toList()
        );
    }

    public void markDelivered(Order order, User deliveryUser) {
        OrderStatus previous = order.getStatus();
        order.setStatus(OrderStatus.DELIVERED);
        addHistory(order, previous, OrderStatus.DELIVERED, deliveryUser, "Order delivered");
        loyaltyService.awardForDeliveredOrder(order);
    }

    private Coupon findCoupon(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Coupon not found"));
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.isActive() || now.isBefore(coupon.getStartsAt()) || now.isAfter(coupon.getExpiresAt())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Coupon is not active");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Coupon usage limit reached");
        }
        return coupon;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (subtotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Order does not meet coupon minimum amount");
        }
        BigDecimal discount = switch (coupon.getDiscountType()) {
            case FIXED -> coupon.getDiscountValue();
            case PERCENTAGE -> subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        };
        if (coupon.getMaxDiscountAmount() != null) {
            discount = discount.min(coupon.getMaxDiscountAmount());
        }
        return discount.min(subtotal);
    }

    private boolean isRestaurantAcceptingOrders(Restaurant restaurant, LocalDateTime now) {
        if (!restaurant.isOpen()) {
            return false;
        }
        short dayOfWeek = (short) now.getDayOfWeek().getValue();
        LocalTime currentTime = now.toLocalTime();
        return restaurantScheduleRepository.findByRestaurantIdAndDayOfWeek(restaurant.getId(), dayOfWeek)
                .filter(schedule -> !schedule.isClosed())
                .filter(schedule -> schedule.getOpensAt() != null && schedule.getClosesAt() != null)
                .filter(schedule -> !currentTime.isBefore(schedule.getOpensAt()))
                .filter(schedule -> currentTime.isBefore(schedule.getClosesAt()))
                .isPresent();
    }

    private void addHistory(Order order, OrderStatus previous, OrderStatus next, User user, String notes) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setPreviousStatus(previous);
        history.setNewStatus(next);
        history.setChangedByUser(user);
        history.setNotes(notes);
        historyRepository.save(history);
    }

    private User currentUser() {
        return userRepository.findByIdWithRole(authenticatedUserProvider.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Authenticated user does not exist"));
    }

    private void validateCanView(Order order) {
        UUID currentUserId = authenticatedUserProvider.getCurrentUserId();
        if (order.getCustomer().getId().equals(currentUserId)
                || order.getRestaurant().getOwner().getId().equals(currentUserId)) {
            return;
        }
        User current = currentUser();
        if (current.getRole().getName() == RoleName.ADMIN) {
            return;
        }
        if (current.getRole().getName() == RoleName.DELIVERY
                && deliveryAssignmentRepository.findByOrderId(order.getId())
                .filter(assignment -> assignment.getDeliveryUser().getId().equals(currentUserId))
                .isPresent()) {
            return;
        }
        throw new BusinessException(HttpStatus.FORBIDDEN, "Order is not visible for current user");
    }

    private void requireCustomerOwnerOrAdmin(Order order) {
        UUID currentUserId = authenticatedUserProvider.getCurrentUserId();
        if (order.getCustomer().getId().equals(currentUserId)) {
            return;
        }
        if (currentUser().getRole().getName() == RoleName.ADMIN) {
            return;
        }
        throw new BusinessException(HttpStatus.FORBIDDEN, "Only the order owner or admin can cancel this order");
    }

    private void requireRestaurantOwnerOrAdmin(Order order) {
        UUID currentUserId = authenticatedUserProvider.getCurrentUserId();
        if (order.getRestaurant().getOwner().getId().equals(currentUserId)) {
            return;
        }
        if (currentUser().getRole().getName() == RoleName.ADMIN) {
            return;
        }
        throw new BusinessException(HttpStatus.FORBIDDEN, "Only the restaurant owner or admin can update this order");
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getRestaurant().getId(),
                order.getDeliveryAddress().getId(),
                order.getStatus(),
                order.getSubtotalAmount(),
                order.getTaxAmount(),
                order.getDeliveryFee(),
                order.getTipAmount(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getEstimatedDeliveryMinutes(),
                order.getDemandMultiplier(),
                order.getDemandMultiplier() != null && order.getDemandMultiplier().compareTo(BigDecimal.ONE) > 0,
                order.getDistanceKm(),
                order.getCreatedAt(),
                order.getItems().stream().map(this::toItem).toList(),
                historyRepository.findByOrderIdOrderByChangedAtAsc(order.getId()).stream().map(this::toHistory).toList()
        );
    }

    private OrderItemResponse toItem(OrderItem item) {
        return new OrderItemResponse(item.getId(), item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal());
    }

    private OrderStatusHistoryResponse toHistory(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(history.getId(), history.getPreviousStatus(), history.getNewStatus(), history.getChangedAt(), history.getNotes());
    }
}
