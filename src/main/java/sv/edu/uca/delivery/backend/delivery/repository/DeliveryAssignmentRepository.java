package sv.edu.uca.delivery.backend.delivery.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    boolean existsByOrderId(UUID orderId);

    boolean existsByDeliveryUserIdAndStatusIn(UUID deliveryUserId, Collection<DeliveryStatus> statuses);

    List<DeliveryAssignment> findAllByDeliveryUserIdOrderByAssignedAtDesc(UUID deliveryUserId);

    Optional<DeliveryAssignment> findByIdAndDeliveryUserId(UUID id, UUID deliveryUserId);

    Optional<DeliveryAssignment> findByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select da from DeliveryAssignment da where da.id = :id")
    Optional<DeliveryAssignment> findWithLockingById(UUID id);

    @Query(value = """
            select u.*
            from users u
            join roles r on r.id = u.role_id
            join lateral (
                select dl.location
                from delivery_locations dl
                where dl.delivery_user_id = u.id
                order by dl.recorded_at desc
                limit 1
            ) latest_location on true
            join orders o on o.id = cast(:orderId as uuid)
            join restaurants restaurant on restaurant.id = o.restaurant_id
            where u.is_active = true
              and r.name = 'DELIVERY'
              and not exists (
                  select 1
                  from delivery_assignments da
                  where da.delivery_user_id = u.id
                    and da.status in ('ASSIGNED', 'PICKED_UP', 'ON_THE_WAY')
            )
            order by ST_Distance(latest_location.location, restaurant.location)
            limit 1
            for update of u skip locked
            """, nativeQuery = true)
    Optional<User> findNearestAvailableDeliveryUser(@Param("orderId") UUID orderId);

    @Query("""
            select u
            from User u
            join fetch u.role r
            where u.active = true
              and r.name = sv.edu.uca.delivery.backend.auth.entity.RoleName.DELIVERY
              and not exists (
                  select 1
                  from DeliveryAssignment da
                  where da.deliveryUser.id = u.id
                    and da.status in :statuses
              )
            order by u.createdAt asc
            limit 1
            """)
    Optional<User> findFirstAvailableDeliveryUser(@Param("statuses") Collection<DeliveryStatus> statuses);
}
