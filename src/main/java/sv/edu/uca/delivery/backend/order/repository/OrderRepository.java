package sv.edu.uca.delivery.backend.order.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
