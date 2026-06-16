package sv.edu.uca.delivery.backend.order.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.uca.delivery.backend.order.entity.Order;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findWithLockingById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o join fetch o.customer where o.id = :id")
    Optional<Order> findWithCustomerByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select distinct o from Order o
            left join fetch o.items
            join fetch o.customer
            join fetch o.restaurant r
            join fetch r.owner
            join fetch o.deliveryAddress
            where o.id = :id
            """)
    Optional<Order> findDetailById(@Param("id") UUID id);

    @Query("""
            select distinct o from Order o
            left join fetch o.items
            join fetch o.restaurant r
            join fetch r.owner
            join fetch o.deliveryAddress
            where o.customer.id = :customerId
            order by o.createdAt desc
            """)
    List<Order> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") UUID customerId);

    @Query("""
            select distinct o from Order o
            left join fetch o.items
            join fetch o.customer
            join fetch o.restaurant r
            join fetch r.owner
            join fetch o.deliveryAddress
            where o.restaurant.owner.id = :ownerId
            order by o.createdAt desc
            """)
    List<Order> findByRestaurantOwnerIdOrderByCreatedAtDesc(@Param("ownerId") UUID ownerId);

    long countByRestaurantId(UUID restaurantId);
}
