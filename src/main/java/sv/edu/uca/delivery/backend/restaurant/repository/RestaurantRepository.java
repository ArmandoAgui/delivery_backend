package sv.edu.uca.delivery.backend.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
