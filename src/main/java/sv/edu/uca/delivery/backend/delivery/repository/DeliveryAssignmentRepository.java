package sv.edu.uca.delivery.backend.delivery.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    boolean existsByOrderId(UUID orderId);

    @EntityGraph(attributePaths = {"order", "order.deliveryAddress", "deliveryUser"})
    Optional<DeliveryAssignment> findByOrderId(UUID orderId);

    @EntityGraph(attributePaths = {"order", "order.deliveryAddress", "deliveryUser"})
    List<DeliveryAssignment> findByDeliveryUserIdAndStatusNotOrderByAssignedAtDesc(
            UUID deliveryUserId,
            DeliveryStatus status
    );
}
