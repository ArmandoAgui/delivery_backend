package sv.edu.uca.delivery.backend.service;

import org.junit.jupiter.api.Test;
import sv.edu.uca.delivery.backend.dto.DeliveryEstimate;
import sv.edu.uca.delivery.backend.entity.Address;
import sv.edu.uca.delivery.backend.entity.Cart;
import sv.edu.uca.delivery.backend.entity.Order;
import sv.edu.uca.delivery.backend.entity.Restaurant;
import sv.edu.uca.delivery.backend.entity.User;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFactoryTest {

    private final OrderFactory orderFactory = new OrderFactory();

    @Test
    void platformCouponOnlyReducesCustomerTotalAndKeepsRestaurantBaseSubtotal() {
        BigDecimal subtotal = new BigDecimal("25.00");
        BigDecimal deliveryFee = new BigDecimal("3.00");
        BigDecimal tip = new BigDecimal("2.00");
        BigDecimal discount = new BigDecimal("10.00");

        Order order = orderFactory.fromCart(
                new User(),
                new Restaurant(),
                new Address(),
                new Cart(),
                subtotal,
                BigDecimal.ZERO,
                new DeliveryEstimate(deliveryFee, 30, false, new BigDecimal("4.50"), BigDecimal.ONE),
                tip,
                discount,
                1L,
                "Coupon funded by platform"
        );

        assertThat(order.getSubtotalAmount()).isEqualByComparingTo("25.00");
        assertThat(order.getDeliveryFee()).isEqualByComparingTo("3.00");
        assertThat(order.getTipAmount()).isEqualByComparingTo("2.00");
        assertThat(order.getDiscountAmount()).isEqualByComparingTo("10.00");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("20.00");
    }
}
