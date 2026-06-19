package sv.edu.uca.delivery.backend.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.restaurant.entity.RestaurantSchedule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantScheduleRepository extends JpaRepository<RestaurantSchedule, Long> {

    List<RestaurantSchedule> findByRestaurantIdOrderByDayOfWeek(UUID restaurantId);

    Optional<RestaurantSchedule> findByRestaurantIdAndDayOfWeek(UUID restaurantId, short dayOfWeek);
}
