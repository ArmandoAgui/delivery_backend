package sv.edu.uca.delivery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.entity.Refund;

import java.util.Optional;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    Optional<Refund> findFirstByComplaintIdOrderByCreatedAtDesc(UUID complaintId);

    Optional<Refund> findFirstByPaymentOrderIdOrderByCreatedAtDesc(UUID orderId);

    boolean existsByComplaintId(UUID complaintId);
}
