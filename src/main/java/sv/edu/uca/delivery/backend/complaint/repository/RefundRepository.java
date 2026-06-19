package sv.edu.uca.delivery.backend.complaint.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.complaint.entity.Refund;

import java.util.Optional;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    Optional<Refund> findFirstByComplaintIdOrderByCreatedAtDesc(UUID complaintId);

    boolean existsByComplaintId(UUID complaintId);
}
