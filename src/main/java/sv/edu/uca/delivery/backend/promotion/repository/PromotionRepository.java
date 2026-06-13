package sv.edu.uca.delivery.backend.promotion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.promotion.entity.Promotion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByRestaurant_Id(UUID restaurantId);

    Optional<Promotion> findByIdAndActiveTrue(Long id);

    boolean existsByRestaurant_IdAndActiveTrue(UUID restaurantId);

    List<Promotion> findByActiveTrue();
}