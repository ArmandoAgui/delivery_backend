package sv.edu.uca.delivery.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.repository.AddressRepository;
import sv.edu.uca.delivery.backend.repository.CouponRepository;
import sv.edu.uca.delivery.backend.repository.LoyaltyAccountRepository;
import sv.edu.uca.delivery.backend.entity.RoleName;
import sv.edu.uca.delivery.backend.dto.AddCartItemRequest;
import sv.edu.uca.delivery.backend.dto.CartItemResponse;
import sv.edu.uca.delivery.backend.dto.CartQuoteRequest;
import sv.edu.uca.delivery.backend.dto.CartQuoteResponse;
import sv.edu.uca.delivery.backend.dto.CartResponse;
import sv.edu.uca.delivery.backend.dto.UpdateCartItemRequest;
import sv.edu.uca.delivery.backend.entity.Address;
import sv.edu.uca.delivery.backend.entity.Cart;
import sv.edu.uca.delivery.backend.entity.CartItem;
import sv.edu.uca.delivery.backend.entity.CartStatus;
import sv.edu.uca.delivery.backend.entity.Coupon;
import sv.edu.uca.delivery.backend.repository.CartItemRepository;
import sv.edu.uca.delivery.backend.repository.CartRepository;
import sv.edu.uca.delivery.backend.exception.BusinessException;
import sv.edu.uca.delivery.backend.util.AppClock;
import sv.edu.uca.delivery.backend.dto.DeliveryEstimate;
import sv.edu.uca.delivery.backend.service.DeliveryEstimateService;
import sv.edu.uca.delivery.backend.entity.Product;
import sv.edu.uca.delivery.backend.repository.ProductRepository;
import sv.edu.uca.delivery.backend.entity.Restaurant;
import sv.edu.uca.delivery.backend.repository.RestaurantScheduleRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.entity.User;
import sv.edu.uca.delivery.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final RestaurantScheduleRepository restaurantScheduleRepository;
    private final DeliveryEstimateService deliveryEstimateService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        return cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(currentCustomerId(), CartStatus.ACTIVE)
                .map(this::toResponse)
                .orElse(new CartResponse(null, null, null, BigDecimal.ZERO, java.util.List.of()));
    }

    @Transactional(readOnly = true)
    public CartQuoteResponse quote(CartQuoteRequest request) {
        UUID customerId = currentCustomerId();
        Cart cart = cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        if (!isRestaurantAcceptingOrders(cart.getRestaurant(), AppClock.now())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Restaurant is currently closed");
        }

        Optional<Address> address = quoteAddress(request.deliveryAddressId(), customerId);
        BigDecimal subtotal = cartSubtotal(cart);
        BigDecimal tip = nullToZero(request.tipAmount()).setScale(2, RoundingMode.HALF_UP);
        Coupon coupon = findCoupon(request.couponCode());
        BigDecimal couponDiscount = coupon == null ? BigDecimal.ZERO : calculateDiscount(coupon, subtotal);
        DeliveryEstimate estimate = deliveryEstimateService.estimateForCart(cart, address);
        BigDecimal totalBeforeWallet = subtotal
                .add(estimate.estimatedDeliveryFee())
                .add(tip)
                .subtract(couponDiscount)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal walletApplied = Boolean.TRUE.equals(request.useDigitalWallet())
                ? digitalWalletBalance(customerId).min(totalBeforeWallet).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal total = totalBeforeWallet.subtract(walletApplied).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        return new CartQuoteResponse(
                cart.getId(),
                cart.getRestaurant().getId(),
                cart.getRestaurant().getName(),
                subtotal,
                estimate.estimatedDeliveryFee(),
                tip,
                couponDiscount,
                walletApplied,
                total,
                coupon == null ? null : coupon.getCode(),
                coupon != null,
                coupon == null ? null : "Coupon applied. The platform funds this discount.",
                estimate.estimatedDeliveryMinutes(),
                estimate.peakDemand(),
                estimate.distanceKm()
        );
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        UUID customerId = currentCustomerId();
        User customer = userRepository.findByIdAndActiveTrueAndRoleName(customerId, RoleName.CUSTOMER)
                .orElseThrow(() -> new BusinessException(HttpStatus.FORBIDDEN, "Only active customers can use carts"));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!product.isAvailable()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Product is not available");
        }
        if (!isRestaurantAcceptingOrders(product.getRestaurant(), AppClock.now())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Restaurant is currently closed");
        }

        Cart cart = cartRepository
                .findFirstByCustomerIdAndRestaurantIdAndStatusOrderByCreatedAtDesc(
                        customerId,
                        product.getRestaurant().getId(),
                        CartStatus.ACTIVE
                )
                .orElseGet(() -> {
                    cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, CartStatus.ACTIVE)
                            .filter(existing -> !existing.getItems().isEmpty())
                            .filter(existing -> !existing.getRestaurant().getId().equals(product.getRestaurant().getId()))
                            .ifPresent(existing -> {
                                throw new BusinessException(HttpStatus.CONFLICT, "Cart already has products from another restaurant");
                            });
                    Cart created = new Cart();
                    created.setCustomer(customer);
                    created.setRestaurant(product.getRestaurant());
                    return created;
                });

        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem created = new CartItem();
                    created.setCart(cart);
                    created.setProduct(product);
                    created.setQuantity(0);
                    created.setUnitPrice(product.getPrice());
                    cart.getItems().add(created);
                    return created;
                });
        item.setQuantity(item.getQuantity() + request.quantity());
        item.setUnitPrice(product.getPrice());
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(UUID itemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByIdAndCartCustomerId(itemId, currentCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cart item not found"));
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return toResponse(item.getCart());
    }

    @Transactional
    public void removeItem(UUID itemId) {
        CartItem item = cartItemRepository.findByIdAndCartCustomerId(itemId, currentCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Cart item not found"));
        Cart cart = item.getCart();
        cart.getItems().removeIf(existing -> existing.getId().equals(itemId));
        cartItemRepository.delete(item);
        if (cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
        }
    }

    @Transactional
    public void clearCart() {
        cartRepository.findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(currentCustomerId(), CartStatus.ACTIVE)
                .ifPresent(cartRepository::delete);
    }

    public CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        DeliveryEstimate estimate = deliveryEstimateService.estimateForCart(
                cart,
                addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(cart.getCustomer().getId()).stream().findFirst()
        );
        return new CartResponse(
                cart.getId(),
                cart.getRestaurant().getId(),
                cart.getRestaurant().getName(),
                subtotal,
                items,
                estimate.estimatedDeliveryFee(),
                estimate.estimatedDeliveryMinutes(),
                estimate.peakDemand(),
                estimate.distanceKm()
        );
    }

    private UUID currentCustomerId() {
        return authenticatedUserProvider.getCurrentUserId();
    }

    private Optional<Address> quoteAddress(UUID addressId, UUID customerId) {
        if (addressId != null) {
            return Optional.of(addressRepository.findByIdAndUserId(addressId, customerId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Delivery address not found")));
        }
        return addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(customerId).stream().findFirst();
    }

    private BigDecimal cartSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Coupon findCoupon(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Coupon not found"));
        LocalDateTime now = AppClock.now();
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
        return discount.min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal digitalWalletBalance(UUID customerId) {
        return loyaltyAccountRepository.findByCustomerId(customerId)
                .map(account -> account.getCreditBalance() == null ? BigDecimal.ZERO : account.getCreditBalance())
                .orElse(BigDecimal.ZERO)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nullToZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
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
}
