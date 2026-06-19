package sv.edu.uca.delivery.backend.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.review.entity.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderIdAndReviewerId(UUID orderId, UUID reviewerId);

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    Optional<Review> findByOrderId(UUID orderId);
}
