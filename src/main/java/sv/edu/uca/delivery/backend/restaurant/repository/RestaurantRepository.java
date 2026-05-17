package sv.edu.uca.delivery.backend.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;

import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByActiveTrue();

    List<Restaurant> findByOpenTrueAndActiveTrue();
}