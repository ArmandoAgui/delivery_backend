package sv.edu.uca.delivery.backend.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import sv.edu.uca.delivery.backend.payment.entity.Payment;
import sv.edu.uca.delivery.backend.payment.entity.PaymentStatus;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(UUID orderId, PaymentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByPaypalOrderId(String paypalOrderId);
}
