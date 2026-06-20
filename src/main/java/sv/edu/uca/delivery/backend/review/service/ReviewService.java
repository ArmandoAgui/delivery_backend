package sv.edu.uca.delivery.backend.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryAssignmentRepository;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.review.dto.CreateReviewRequest;
import sv.edu.uca.delivery.backend.review.dto.ReviewResponse;
import sv.edu.uca.delivery.backend.review.entity.Review;
import sv.edu.uca.delivery.backend.review.repository.ReviewRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public ReviewResponse create(CreateReviewRequest request) {
        UUID customerId = authenticatedUserProvider.getCurrentUserId();
        Order order = orderRepository.findDetailById(request.orderId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only the order owner can review");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(HttpStatus.CONFLICT, "Only delivered orders can be reviewed");
        }
        if (reviewRepository.existsByOrderIdAndReviewerId(order.getId(), customerId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Order already has a review from this customer");
        }
        User reviewer = userRepository.findByIdAndActiveTrue(customerId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Authenticated user does not exist"));
        Review review = new Review();
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setRestaurant(order.getRestaurant());
        deliveryAssignmentRepository.findByOrderId(order.getId())
                .ifPresent(assignment -> review.setDeliveryUser(assignment.getDeliveryUser()));
        review.setRating(request.rating());
        review.setComment(request.comment());
        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> byRestaurant(UUID restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> byDeliveryUser(UUID deliveryUserId) {
        return reviewRepository.findByDeliveryUserIdOrderByCreatedAtDesc(deliveryUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrder().getId(),
                review.getReviewer().getId(),
                review.getRestaurant().getId(),
                review.getDeliveryUser() == null ? null : review.getDeliveryUser().getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
