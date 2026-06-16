package sv.edu.uca.delivery.backend.loyalty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.loyalty.entity.LoyaltyTransaction;

import java.util.List;
import java.util.UUID;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {

    List<LoyaltyTransaction> findByAccountCustomerIdOrderByCreatedAtDesc(UUID customerId);

    boolean existsByOrderIdAndTransactionType(UUID orderId, sv.edu.uca.delivery.backend.loyalty.entity.LoyaltyTransactionType type);
}
