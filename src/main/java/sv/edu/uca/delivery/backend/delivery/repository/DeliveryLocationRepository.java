package sv.edu.uca.delivery.backend.delivery.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryLocation;

public interface DeliveryLocationRepository extends JpaRepository<DeliveryLocation, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO delivery_locations (
                delivery_user_id,
                order_id,
                delivery_batch_id,
                location,
                recorded_at,
                created_at
            )
            VALUES (
                :deliveryUserId,
                :orderId,
                :deliveryBatchId,
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                NOW(),
                NOW()
            )
            """, nativeQuery = true)
    void insertLocation(
            @Param("deliveryUserId") UUID deliveryUserId,
            @Param("orderId") UUID orderId,
            @Param("deliveryBatchId") UUID deliveryBatchId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude
    );

    @Query(value = """
            SELECT
                ST_Y(dl.location::geometry) AS latitude,
                ST_X(dl.location::geometry) AS longitude,
                CASE
                    WHEN a.location IS NULL THEN NULL
                    ELSE ST_Distance(dl.location, a.location)
                END AS distanceToDestinationMeters,
                dl.recorded_at AS recordedAt
            FROM delivery_locations dl
            JOIN orders o ON o.id = dl.order_id
            JOIN addresses a ON a.id = o.delivery_address_id
            WHERE dl.order_id = :orderId
            ORDER BY dl.recorded_at DESC, dl.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<DeliveryLocationProjection> findLatestByOrderId(@Param("orderId") UUID orderId);

    @Query(value = """
            SELECT
                u.id AS deliveryUserId,
                CONCAT(u.first_name, ' ', u.last_name) AS fullName,
                u.email AS email,
                ST_Distance(
                    latest_location.location,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
                ) AS distanceMeters
            FROM users u
            JOIN roles r ON r.id = u.role_id
            JOIN LATERAL (
                SELECT dl.location
                FROM delivery_locations dl
                WHERE dl.delivery_user_id = u.id
                ORDER BY dl.recorded_at DESC, dl.id DESC
                LIMIT 1
            ) latest_location ON TRUE
            WHERE r.name = 'DELIVERY'
              AND u.is_active = TRUE
              AND NOT EXISTS (
                  SELECT 1
                  FROM delivery_assignments da
                  WHERE da.delivery_user_id = u.id
                    AND da.status IN ('ASSIGNED', 'ACCEPTED', 'PICKED_UP')
              )
              AND ST_DWithin(
                  latest_location.location,
                  ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                  :radiusMeters
              )
            ORDER BY distanceMeters ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<NearbyDeliveryProjection> findNearestAvailableDelivery(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Integer radiusMeters
    );
}
