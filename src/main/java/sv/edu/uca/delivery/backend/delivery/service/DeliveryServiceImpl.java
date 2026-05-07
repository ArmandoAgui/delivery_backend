package sv.edu.uca.delivery.backend.delivery.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.delivery.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryAssignmentResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryLocationRequest;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryLocationResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryOrderResponse;
import sv.edu.uca.delivery.backend.delivery.dto.DeliveryTrackingResponse;
import sv.edu.uca.delivery.backend.delivery.dto.NearbyDeliveryResponse;
import sv.edu.uca.delivery.backend.delivery.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.delivery.exception.DeliveryException;
import sv.edu.uca.delivery.backend.delivery.mapper.DeliveryMapper;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryAssignmentRepository;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryBatchRepository;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryLocationProjection;
import sv.edu.uca.delivery.backend.delivery.repository.DeliveryLocationRepository;
import sv.edu.uca.delivery.backend.delivery.repository.NearbyDeliveryProjection;
import sv.edu.uca.delivery.backend.delivery.entity.DeliveryBatch;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private static final String DELIVERY_ROLE = "DELIVERY";
    private static final int DEFAULT_RADIUS_METERS = 5_000;
    private static final Set<DeliveryStatus> FINAL_STATUSES = Set.of(DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED);
    private static final Map<DeliveryStatus, Set<DeliveryStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(DeliveryStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(DeliveryStatus.ASSIGNED, Set.of(DeliveryStatus.ACCEPTED, DeliveryStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.ACCEPTED, Set.of(DeliveryStatus.PICKED_UP, DeliveryStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.PICKED_UP, Set.of(DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(DeliveryStatus.DELIVERED, Set.of());
        ALLOWED_TRANSITIONS.put(DeliveryStatus.CANCELLED, Set.of());
    }

    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryBatchRepository batchRepository;
    private final DeliveryLocationRepository locationRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryMapper mapper;

    public DeliveryServiceImpl(
            DeliveryAssignmentRepository assignmentRepository,
            DeliveryBatchRepository batchRepository,
            DeliveryLocationRepository locationRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            DeliveryMapper mapper
    ) {
        this.assignmentRepository = assignmentRepository;
        this.batchRepository = batchRepository;
        this.locationRepository = locationRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public DeliveryAssignmentResponse assignDelivery(AssignDeliveryRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> notFound("Order not found"));
        validateAssignableOrder(order);

        if (assignmentRepository.existsByOrderId(order.getId())) {
            throw conflict("Order already has a delivery assignment");
        }

        User deliveryUser = resolveDeliveryUser(request);
        validateDeliveryUser(deliveryUser);

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setOrder(order);
        assignment.setDeliveryUser(deliveryUser);
        assignment.setStatus(DeliveryStatus.ASSIGNED);

        return mapper.toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOrderResponse> getMyOrders(Authentication authentication) {
        User deliveryUser = getAuthenticatedDelivery(authentication);
        return assignmentRepository
                .findByDeliveryUserIdAndStatusNotOrderByAssignedAtDesc(deliveryUser.getId(), DeliveryStatus.CANCELLED)
                .stream()
                .map(mapper::toDeliveryOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public DeliveryAssignmentResponse updateStatus(
            UUID assignmentId,
            UpdateDeliveryStatusRequest request,
            Authentication authentication
    ) {
        User deliveryUser = getAuthenticatedDelivery(authentication);
        DeliveryAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> notFound("Delivery assignment not found"));

        if (!assignment.getDeliveryUser().getId().equals(deliveryUser.getId())) {
            throw forbidden("Only the assigned delivery user can update this delivery");
        }

        validateStatusTransition(assignment, request.getStatus());
        applyStatus(assignment, request.getStatus());

        return mapper.toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public DeliveryLocationResponse registerLocation(DeliveryLocationRequest request, Authentication authentication) {
        User deliveryUser = getAuthenticatedDelivery(authentication);
        validateTrackingTarget(request);

        Order order = null;
        DeliveryBatch batch = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> notFound("Order not found"));
            DeliveryAssignment assignment = assignmentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> notFound("Delivery assignment not found for order"));
            if (!assignment.getDeliveryUser().getId().equals(deliveryUser.getId())) {
                throw forbidden("Only the assigned delivery user can register location for this order");
            }
            if (assignment.getStatus() == DeliveryStatus.CANCELLED || assignment.getStatus() == DeliveryStatus.DELIVERED) {
                throw conflict("Cannot register location for a finalized delivery");
            }
        }
        if (request.getDeliveryBatchId() != null) {
            batch = batchRepository.findById(request.getDeliveryBatchId())
                    .orElseThrow(() -> notFound("Delivery batch not found"));
            if (!batch.getDeliveryUser().getId().equals(deliveryUser.getId())) {
                throw forbidden("Only the assigned delivery user can register location for this batch");
            }
        }

        locationRepository.insertLocation(
                deliveryUser.getId(),
                order == null ? null : order.getId(),
                batch == null ? null : batch.getId(),
                request.getLatitude(),
                request.getLongitude()
        );

        DeliveryLocationProjection latest = order == null
                ? new SavedLocationProjection(request.getLatitude(), request.getLongitude(), null, LocalDateTime.now())
                : locationRepository.findLatestByOrderId(order.getId())
                        .orElse(new SavedLocationProjection(request.getLatitude(), request.getLongitude(), null, LocalDateTime.now()));

        return mapper.toLocationResponse(
                deliveryUser.getId(),
                request.getOrderId(),
                request.getDeliveryBatchId(),
                latest
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryTrackingResponse getTracking(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> notFound("Order not found"));
        DeliveryAssignment assignment = assignmentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> notFound("Delivery assignment not found for order"));
        Optional<DeliveryLocationProjection> latestLocation = locationRepository.findLatestByOrderId(order.getId());
        return mapper.toTrackingResponse(assignment, latestLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public NearbyDeliveryResponse findNearestDelivery(Double latitude, Double longitude, Integer radiusMeters) {
        validateCoordinates(latitude, longitude);
        NearbyDeliveryProjection projection = locationRepository.findNearestAvailableDelivery(
                        latitude,
                        longitude,
                        radiusMeters == null ? DEFAULT_RADIUS_METERS : radiusMeters
                )
                .orElseThrow(() -> notFound("No nearby active delivery user found"));
        return mapper.toNearbyDeliveryResponse(projection);
    }

    private User resolveDeliveryUser(AssignDeliveryRequest request) {
        if (request.getDeliveryUserId() != null) {
            return userRepository.findById(request.getDeliveryUserId())
                    .orElseThrow(() -> notFound("Delivery user not found"));
        }

        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw badRequest("deliveryUserId or latitude/longitude must be provided");
        }

        NearbyDeliveryResponse nearest = findNearestDelivery(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusMeters()
        );
        return userRepository.findById(nearest.getDeliveryUserId())
                .orElseThrow(() -> notFound("Delivery user not found"));
    }

    private void validateAssignableOrder(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw conflict("Cancelled orders cannot be assigned");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw conflict("Delivered orders cannot be assigned");
        }
    }

    private void validateDeliveryUser(User deliveryUser) {
        if (deliveryUser.getRole() == null || !DELIVERY_ROLE.equals(deliveryUser.getRole().getName())) {
            throw badRequest("User must have DELIVERY role");
        }
        if (!Boolean.TRUE.equals(deliveryUser.getActive())) {
            throw conflict("Delivery user is not active");
        }
    }

    private User getAuthenticatedDelivery(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw forbidden("Authenticated delivery user is required");
        }

        Optional<User> user = resolveAuthenticatedUser(authentication.getName());

        User deliveryUser = user.orElseThrow(() -> forbidden("Authenticated user not found"));
        validateDeliveryUser(deliveryUser);
        return deliveryUser;
    }

    private Optional<User> resolveAuthenticatedUser(String subject) {
        try {
            return userRepository.findById(UUID.fromString(subject));
        } catch (IllegalArgumentException ignored) {
            return userRepository.findByEmail(subject);
        }
    }

    private void validateStatusTransition(DeliveryAssignment assignment, DeliveryStatus nextStatus) {
        DeliveryStatus currentStatus = assignment.getStatus();
        if (FINAL_STATUSES.contains(currentStatus)) {
            throw conflict("Cannot update a finalized delivery");
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(nextStatus)) {
            throw conflict("Invalid delivery status transition from " + currentStatus + " to " + nextStatus);
        }
        if (nextStatus == DeliveryStatus.DELIVERED && currentStatus != DeliveryStatus.PICKED_UP) {
            throw conflict("Cannot deliver an order that has not been picked up");
        }
    }

    private void applyStatus(DeliveryAssignment assignment, DeliveryStatus nextStatus) {
        assignment.setStatus(nextStatus);
        if (nextStatus == DeliveryStatus.PICKED_UP) {
            assignment.setPickedUpAt(LocalDateTime.now());
            assignment.getOrder().setStatus(OrderStatus.ON_THE_WAY);
        }
        if (nextStatus == DeliveryStatus.DELIVERED) {
            assignment.setDeliveredAt(LocalDateTime.now());
            assignment.getOrder().setStatus(OrderStatus.DELIVERED);
        }
        if (nextStatus == DeliveryStatus.CANCELLED) {
            assignment.getOrder().setStatus(OrderStatus.CANCELLED);
        }
    }

    private void validateTrackingTarget(DeliveryLocationRequest request) {
        if (request.getOrderId() == null && request.getDeliveryBatchId() == null) {
            throw badRequest("orderId or deliveryBatchId is required");
        }
        if (request.getOrderId() != null && request.getDeliveryBatchId() != null) {
            throw badRequest("Only one tracking target is allowed");
        }
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw badRequest("latitude and longitude are required");
        }
    }

    private DeliveryException badRequest(String message) {
        return new DeliveryException(HttpStatus.BAD_REQUEST, message);
    }

    private DeliveryException forbidden(String message) {
        return new DeliveryException(HttpStatus.FORBIDDEN, message);
    }

    private DeliveryException notFound(String message) {
        return new DeliveryException(HttpStatus.NOT_FOUND, message);
    }

    private DeliveryException conflict(String message) {
        return new DeliveryException(HttpStatus.CONFLICT, message);
    }

    private record SavedLocationProjection(
            Double latitude,
            Double longitude,
            Double distanceToDestinationMeters,
            LocalDateTime recordedAt
    ) implements DeliveryLocationProjection {

        @Override
        public Double getLatitude() {
            return latitude;
        }

        @Override
        public Double getLongitude() {
            return longitude;
        }

        @Override
        public Double getDistanceToDestinationMeters() {
            return distanceToDestinationMeters;
        }

        @Override
        public LocalDateTime getRecordedAt() {
            return recordedAt;
        }
    }
}
