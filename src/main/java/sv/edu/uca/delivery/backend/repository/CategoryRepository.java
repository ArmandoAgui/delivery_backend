package sv.edu.uca.delivery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrue();

    Optional<Category> findByIdAndActiveTrue(Long id);

    List<Category> findByRestaurant_IdAndActiveTrue(UUID restaurantId);

    boolean existsByRestaurant_IdAndNameIgnoreCase(
            UUID restaurantId,
            String name
    );
}