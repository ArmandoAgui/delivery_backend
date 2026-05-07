package sv.edu.uca.delivery.backend.delivery.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryBatch;

public interface DeliveryBatchRepository extends JpaRepository<DeliveryBatch, UUID> {
}
