package sv.edu.uca.delivery.backend.cart.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.cart.entity.Cart;
import sv.edu.uca.delivery.backend.cart.entity.CartStatus;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = {"items", "items.product", "restaurant", "customer"})
    Optional<Cart> findFirstByCustomerIdAndStatusOrderByCreatedAtDesc(UUID customerId, CartStatus status);

    @EntityGraph(attributePaths = {"items", "items.product", "restaurant", "customer"})
    Optional<Cart> findFirstByCustomerIdAndRestaurantIdAndStatusOrderByCreatedAtDesc(
            UUID customerId,
            UUID restaurantId,
            CartStatus status
    );
}
