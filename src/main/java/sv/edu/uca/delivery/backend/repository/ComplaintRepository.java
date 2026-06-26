package sv.edu.uca.delivery.backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.uca.delivery.backend.entity.Complaint;
import sv.edu.uca.delivery.backend.entity.ComplaintStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    boolean existsByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Complaint c join fetch c.order join fetch c.customer where c.id = :id")
    Optional<Complaint> findWithOrderAndCustomerByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select c from Complaint c
            join fetch c.order
            join fetch c.customer
            where (:customerUserId is null or c.customer.id = :customerUserId)
              and (:status is null or c.status = :status)
              and (:orderId is null or c.order.id = :orderId)
            order by c.createdAt desc
            """)
    List<Complaint> findAllFiltered(
            @Param("customerUserId") UUID customerUserId,
            @Param("status") ComplaintStatus status,
            @Param("orderId") UUID orderId
    );

    @Query("select c from Complaint c join fetch c.order join fetch c.customer where c.id = :id")
    Optional<Complaint> findDetailById(@Param("id") UUID id);
}
