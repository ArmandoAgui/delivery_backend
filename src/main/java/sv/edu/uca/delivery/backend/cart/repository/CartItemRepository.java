package sv.edu.uca.delivery.backend.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.cart.entity.CartItem;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByIdAndCartCustomerId(UUID id, UUID customerId);
}
