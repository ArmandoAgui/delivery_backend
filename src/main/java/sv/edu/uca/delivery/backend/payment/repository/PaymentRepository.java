package sv.edu.uca.delivery.backend.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.payment.entity.Payment;
import sv.edu.uca.delivery.backend.payment.entity.PaymentStatus;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(UUID orderId, PaymentStatus status);
}
