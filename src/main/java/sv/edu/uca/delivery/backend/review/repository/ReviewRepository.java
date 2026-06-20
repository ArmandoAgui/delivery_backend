package sv.edu.uca.delivery.backend.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sv.edu.uca.delivery.backend.review.entity.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderIdAndReviewerId(UUID orderId, UUID reviewerId);

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    List<Review> findByDeliveryUserIdOrderByCreatedAtDesc(UUID deliveryUserId);

    Optional<Review> findByOrderId(UUID orderId);

    long countByRestaurantId(UUID restaurantId);

    long countByDeliveryUserId(UUID deliveryUserId);

    @Query("select avg(r.rating) from Review r where r.restaurant.id = :restaurantId")
    Double averageRatingByRestaurantId(UUID restaurantId);

    @Query("select avg(r.rating) from Review r where r.deliveryUser.id = :deliveryUserId")
    Double averageRatingByDeliveryUserId(UUID deliveryUserId);
}
