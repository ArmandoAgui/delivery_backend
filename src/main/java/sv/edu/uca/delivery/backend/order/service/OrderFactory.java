package sv.edu.uca.delivery.backend.order.service;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.address.entity.Address;
import sv.edu.uca.delivery.backend.cart.entity.Cart;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryEstimate;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.user.entity.User;

import java.math.BigDecimal;

@Component
public class OrderFactory {

    public Order fromCart(
            User customer,
            Restaurant restaurant,
            Address address,
            Cart cart,
            BigDecimal subtotal,
            BigDecimal tax,
            DeliveryEstimate deliveryEstimate,
            BigDecimal tip,
            BigDecimal discount,
            Long couponId,
            String notes
    ) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(address);
        order.setSubtotalAmount(subtotal);
        order.setTaxAmount(tax);
        order.setDeliveryFee(deliveryEstimate.estimatedDeliveryFee());
        order.setTipAmount(tip);
        order.setDiscountAmount(discount);
        order.setCouponId(couponId);
        order.setEstimatedDeliveryMinutes(deliveryEstimate.estimatedDeliveryMinutes());
        order.setDemandMultiplier(deliveryEstimate.demandMultiplier());
        order.setDistanceKm(deliveryEstimate.distanceKm());
        order.setTotalAmount(subtotal.add(tax).add(deliveryEstimate.estimatedDeliveryFee()).add(tip).subtract(discount));
        order.setNotes(notes);
        return order;
    }
}
