package sv.edu.uca.delivery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sv.edu.uca.delivery.backend.entity.Review;
import sv.edu.uca.delivery.backend.entity.ReviewType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderIdAndReviewerIdAndReviewType(UUID orderId, UUID reviewerId, ReviewType reviewType);

    boolean existsByOrderIdAndReviewerIdAndReviewTypeAndProductId(UUID orderId, UUID reviewerId, ReviewType reviewType, UUID productId);

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    List<Review> findByDeliveryUserIdOrderByCreatedAtDesc(UUID deliveryUserId);

    List<Review> findByProductIdOrderByCreatedAtDesc(UUID productId);

    Optional<Review> findByOrderId(UUID orderId);

    @Query("select count(r) from Review r where r.restaurant.id = :restaurantId and r.reviewType = 'RESTAURANT'")
    long countByRestaurantId(UUID restaurantId);

    @Query("select count(r) from Review r where r.deliveryUser.id = :deliveryUserId and r.reviewType = 'DELIVERY'")
    long countByDeliveryUserId(UUID deliveryUserId);

    @Query("select avg(r.rating) from Review r where r.restaurant.id = :restaurantId and r.reviewType = 'RESTAURANT'")
    Double averageRatingByRestaurantId(UUID restaurantId);

    @Query("select avg(r.rating) from Review r where r.deliveryUser.id = :deliveryUserId and r.reviewType = 'DELIVERY'")
    Double averageRatingByDeliveryUserId(UUID deliveryUserId);
}
