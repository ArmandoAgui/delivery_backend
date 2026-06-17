package sv.edu.uca.delivery.backend.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByActiveTrue();

    @Query("""
            select count(r) > 0
            from Restaurant r
            where r.owner.id = :ownerId
            """)
    boolean existsByOwnerId(UUID ownerId);

    Optional<Restaurant> findByIdAndActiveTrue(UUID id);

    List<Restaurant> findByOpenTrueAndActiveTrue();

    @Query("""
            select r
            from Restaurant r
            where r.active = true
              and (
                    lower(r.name) like lower(concat('%', :query, '%'))
                 or lower(r.city) like lower(concat('%', :query, '%'))
                 or lower(coalesce(r.description, '')) like lower(concat('%', :query, '%'))
              )
            order by r.name asc
            """)
    List<Restaurant> searchActive(@Param("query") String query);

    @Query(value = """
            select ST_Distance(r.location, a.location) / 1000.0
            from restaurants r
            join addresses a on a.id = cast(:addressId as uuid)
            where r.id = cast(:restaurantId as uuid)
              and r.location is not null
              and a.location is not null
            """, nativeQuery = true)
    Double distanceKmBetweenRestaurantAndAddress(
            @Param("restaurantId") UUID restaurantId,
            @Param("addressId") UUID addressId
    );

    @Query(value = """
            select r.*
            from restaurants r
            where r.is_active = true
              and r.location is not null
              and ST_DWithin(
                    r.location,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                    :radiusMeters
              )
            order by ST_Distance(
                    r.location,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
              )
            """, nativeQuery = true)
    List<Restaurant> findNearby(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters
    );
}
