package sv.edu.uca.delivery.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.entity.RoleName;
import sv.edu.uca.delivery.backend.exception.BusinessException;
import sv.edu.uca.delivery.backend.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.dto.DeliveryProfileResponse;
import sv.edu.uca.delivery.backend.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.dto.DeliveryStatsResponse;
import sv.edu.uca.delivery.backend.dto.UpdateDeliveryAvailabilityRequest;
import sv.edu.uca.delivery.backend.dto.UpdateDeliveryLocationRequest;
import sv.edu.uca.delivery.backend.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.entity.DeliveryAssignment;
import sv.edu.uca.delivery.backend.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.exception.DeliveryBusinessException;
import sv.edu.uca.delivery.backend.exception.DeliveryNotFoundException;
import sv.edu.uca.delivery.backend.mapper.DeliveryMapper;
import sv.edu.uca.delivery.backend.repository.DeliveryAssignmentRepository;
import sv.edu.uca.delivery.backend.entity.Order;
import sv.edu.uca.delivery.backend.entity.OrderStatus;
import sv.edu.uca.delivery.backend.repository.OrderRepository;
import sv.edu.uca.delivery.backend.service.OrderService;
import sv.edu.uca.delivery.backend.repository.ReviewRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.entity.User;
import sv.edu.uca.delivery.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeliveryService {

    private static final List<DeliveryStatus> ACTIVE_STATUSES = List.of(
            DeliveryStatus.ASSIGNED,
            DeliveryStatus.PICKED_UP,
            DeliveryStatus.ON_THE_WAY
    );

    private static final List<DeliveryStatus> HISTORY_STATUSES = List.of(
            DeliveryStatus.DELIVERED,
            DeliveryStatus.CANCELLED,
            DeliveryStatus.REJECTED
    );

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryMapper deliveryMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final OrderService orderService;
    private final JdbcTemplate jdbcTemplate;
    private final ReviewRepository reviewRepository;

    @Autowired
    public DeliveryService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            DeliveryAssignmentRepository deliveryAssignmentRepository,
            DeliveryMapper deliveryMapper,
            AuthenticatedUserProvider authenticatedUserProvider,
            OrderService orderService,
            JdbcTemplate jdbcTemplate,
            ReviewRepository reviewRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.deliveryMapper = deliveryMapper;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.orderService = orderService;
        this.jdbcTemplate = jdbcTemplate;
        this.reviewRepository = reviewRepository;
    }

    public DeliveryService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            DeliveryAssignmentRepository deliveryAssignmentRepository,
            DeliveryMapper deliveryMapper,
            AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.deliveryMapper = deliveryMapper;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.orderService = null;
        this.jdbcTemplate = null;
        this.reviewRepository = null;
    }

    @Transactional
    public DeliveryResponse assignDelivery(AssignDeliveryRequest request) {
        throw new BusinessException(HttpStatus.FORBIDDEN, "Manual delivery assignment is disabled; confirm the order to assign automatically");
    }

    @Transactional
    public DeliveryResponse assignAutomatically(Order order) {
        validateOrderCanBeAssigned(order);
        if (deliveryAssignmentRepository.existsByOrderId(order.getId())) {
            throw new DeliveryBusinessException("Order already has a delivery assignment");
        }

        User deliveryUser = findNextCandidate(order.getId())
                .orElseThrow(() -> new DeliveryBusinessException("No available delivery user was found"));

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setOrder(order);
        assignment.setDeliveryUser(deliveryUser);
        assignment.setStatus(DeliveryStatus.OFFERED);

        return deliveryMapper.toResponse(deliveryAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getMyOrders() {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);

        return deliveryAssignmentRepository.findAllByDeliveryUserIdOrderByAssignedAtDesc(deliveryUserId)
                .stream()
                .map(deliveryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getMyRequests() {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);
        return deliveryAssignmentRepository.findAllByDeliveryUserIdAndStatusOrderByAssignedAtDesc(deliveryUserId, DeliveryStatus.OFFERED)
                .stream()
                .map(deliveryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getMyActiveDeliveries() {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);
        return deliveryAssignmentRepository.findAllByDeliveryUserIdAndStatusInOrderByAssignedAtDesc(deliveryUserId, ACTIVE_STATUSES)
                .stream()
                .map(deliveryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getMyHistory() {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);
        return deliveryAssignmentRepository.findAllByDeliveryUserIdAndStatusInOrderByAssignedAtDesc(deliveryUserId, HISTORY_STATUSES)
                .stream()
                .map(deliveryMapper::toResponse)
                .toList();
    }

    @Transactional
    public DeliveryResponse acceptRequest(UUID deliveryAssignmentId) {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);
        DeliveryAssignment assignment = findOwnedAssignmentForUpdate(deliveryAssignmentId, deliveryUserId);
        if (assignment.getStatus() != DeliveryStatus.OFFERED) {
            throw new DeliveryBusinessException("Only offered delivery requests can be accepted");
        }
        assignment.setStatus(DeliveryStatus.ASSIGNED);
        assignment.setAssignedAt(LocalDateTime.now());
        return deliveryMapper.toResponse(deliveryAssignmentRepository.save(assignment));
    }

    @Transactional
    public DeliveryResponse rejectRequest(UUID deliveryAssignmentId) {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);
        DeliveryAssignment assignment = findOwnedAssignmentForUpdate(deliveryAssignmentId, deliveryUserId);
        if (assignment.getStatus() != DeliveryStatus.OFFERED) {
            throw new DeliveryBusinessException("Only offered delivery requests can be rejected");
        }

        registerRejection(assignment.getOrder().getId(), deliveryUserId);
        Optional<User> nextCandidate = findNextCandidate(assignment.getOrder().getId());
        if (nextCandidate.isPresent()) {
            assignment.setDeliveryUser(nextCandidate.get());
            assignment.setStatus(DeliveryStatus.OFFERED);
            assignment.setAssignedAt(LocalDateTime.now());
            return deliveryMapper.toResponse(deliveryAssignmentRepository.save(assignment));
        }

        assignment.setStatus(DeliveryStatus.REJECTED);
        DeliveryAssignment rejectedAssignment = deliveryAssignmentRepository.save(assignment);
        if (orderService != null) {
            orderService.markRejectedNoDriverAndRefund(rejectedAssignment.getOrder(), rejectedAssignment.getDeliveryUser());
        } else {
            rejectedAssignment.getOrder().setStatus(OrderStatus.REJECTED);
        }
        return deliveryMapper.toResponse(rejectedAssignment);
    }

    @Transactional
    public DeliveryResponse updateStatus(UUID deliveryAssignmentId, UpdateDeliveryStatusRequest request) {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);

        DeliveryAssignment assignment = findOwnedAssignmentForUpdate(deliveryAssignmentId, deliveryUserId);

        validateStatusChange(assignment, request.status());
        applyStatusChange(assignment, request.status());

        return deliveryMapper.toResponse(deliveryAssignmentRepository.save(assignment));
    }

    @Transactional
    public DeliveryProfileResponse updateLocation(UpdateDeliveryLocationRequest request) {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        User deliveryUser = validateDeliveryUser(deliveryUserId);
        requireJdbc();
        jdbcTemplate.update("""
                insert into delivery_profiles (delivery_user_id, is_available, updated_at)
                values (cast(? as uuid), ?, now())
                on conflict (delivery_user_id) do update set
                    is_available = excluded.is_available,
                    updated_at = now()
                """, deliveryUserId, request.available() == null || request.available());
        jdbcTemplate.update("""
                insert into delivery_locations (id, delivery_user_id, location, recorded_at, created_at)
                values (cast(? as uuid), cast(? as uuid), ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, now(), now())
                """, UUID.randomUUID(), deliveryUserId, request.longitude(), request.latitude());
        return getProfile(deliveryUser);
    }

    @Transactional
    public DeliveryProfileResponse updateAvailability(UpdateDeliveryAvailabilityRequest request) {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        User deliveryUser = validateDeliveryUser(deliveryUserId);
        requireJdbc();
        jdbcTemplate.update("""
                insert into delivery_profiles (delivery_user_id, is_available, updated_at)
                values (cast(? as uuid), ?, now())
                on conflict (delivery_user_id) do update set
                    is_available = excluded.is_available,
                    updated_at = now()
                """, deliveryUserId, request.available());
        return getProfile(deliveryUser);
    }

    @Transactional(readOnly = true)
    public DeliveryProfileResponse getMyProfile() {
        return getProfile(validateDeliveryUser(authenticatedUserProvider.getCurrentUserId()));
    }

    @Transactional(readOnly = true)
    public DeliveryStatsResponse getMyStats() {
        UUID deliveryUserId = authenticatedUserProvider.getCurrentUserId();
        validateDeliveryUser(deliveryUserId);
        requireJdbc();
        return jdbcTemplate.queryForObject("""
                with earnings as (
                    select coalesce(sum(o.delivery_fee), 0) as delivery_fees,
                           coalesce(sum(o.tip_amount), 0) as tips,
                           coalesce(sum(da.delivery_gross_earnings), 0) as gross_earnings,
                           coalesce(sum(da.delivery_platform_commission_amount), 0) as platform_commission_amount,
                           coalesce(sum(da.delivery_net_earnings), 0) as net_earnings,
                           avg(da.delivery_platform_commission_percentage) as average_commission_percentage
                    from delivery_assignments da
                    join orders o on o.id = da.order_id
                    where da.delivery_user_id = cast(? as uuid)
                      and da.status = 'DELIVERED'
                ),
                commission as (
                    select coalesce(pc.delivery_commission_percentage, 0) as percentage
                    from platform_commissions pc
                    where pc.starts_at <= now()
                      and (pc.ends_at is null or pc.ends_at > now())
                    order by pc.starts_at desc
                    limit 1
                )
                select
                    (select count(*) from delivery_assignments where delivery_user_id = cast(? as uuid) and status = 'OFFERED') as pending_requests,
                    (select count(*) from delivery_assignments where delivery_user_id = cast(? as uuid) and status in ('ASSIGNED', 'PICKED_UP', 'ON_THE_WAY')) as active_deliveries,
                    (select count(*) from delivery_assignments where delivery_user_id = cast(? as uuid) and status = 'DELIVERED') as completed_deliveries,
                    (select count(*) from delivery_assignment_rejections where delivery_user_id = cast(? as uuid)) as rejected_requests,
                    earnings.delivery_fees as estimated_delivery_earnings,
                    earnings.tips as tips_received,
                    coalesce(earnings.average_commission_percentage, commission.percentage, 0) as platform_commission_percentage,
                    earnings.gross_earnings as gross_earnings,
                    earnings.platform_commission_amount as platform_commission_amount,
                    earnings.net_earnings as net_earnings
                from earnings
                left join commission on true
                """, (rs, rowNum) -> new DeliveryStatsResponse(
                        rs.getLong("pending_requests"),
                        rs.getLong("active_deliveries"),
                        rs.getLong("completed_deliveries"),
                        rs.getLong("rejected_requests"),
                        rs.getBigDecimal("estimated_delivery_earnings"),
                        rs.getBigDecimal("tips_received"),
                        rs.getBigDecimal("platform_commission_percentage"),
                        rs.getBigDecimal("gross_earnings"),
                        rs.getBigDecimal("platform_commission_amount"),
                        rs.getBigDecimal("net_earnings")
                ), deliveryUserId, deliveryUserId, deliveryUserId, deliveryUserId, deliveryUserId);
    }

    private void validateOrderCanBeAssigned(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new DeliveryBusinessException("Cancelled orders cannot be assigned");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new DeliveryBusinessException("Delivered orders cannot be assigned");
        }
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new DeliveryBusinessException("Only confirmed or ready orders can be assigned");
        }
    }

    private User validateDeliveryUser(UUID deliveryUserId) {
        return userRepository.findActiveUserByIdAndRole(deliveryUserId, RoleName.DELIVERY)
                .orElseThrow(() -> new DeliveryBusinessException("Delivery user must exist, be active, and have DELIVERY role"));
    }

    private void validateStatusChange(DeliveryAssignment assignment, DeliveryStatus requestedStatus) {
        if (requestedStatus == DeliveryStatus.CANCELLED) {
            throw new DeliveryBusinessException("Delivery cancellation is not supported by this endpoint");
        }
        if (assignment.getStatus() == DeliveryStatus.OFFERED) {
            throw new DeliveryBusinessException("Delivery request must be accepted before status updates");
        }
        if (assignment.getStatus() == DeliveryStatus.REJECTED) {
            throw new DeliveryBusinessException("Rejected delivery requests cannot be updated");
        }
        if (assignment.getOrder().getStatus() == OrderStatus.CANCELLED) {
            throw new DeliveryBusinessException("Cancelled orders cannot be delivered or updated");
        }
        if (assignment.getStatus() == DeliveryStatus.DELIVERED) {
            throw new DeliveryBusinessException("Delivered assignments cannot be updated");
        }
        if (!isValidTransition(assignment.getStatus(), requestedStatus)) {
            throw new DeliveryBusinessException("Invalid delivery status transition");
        }
    }

    private boolean isValidTransition(DeliveryStatus currentStatus, DeliveryStatus requestedStatus) {
        return (currentStatus == DeliveryStatus.ASSIGNED && requestedStatus == DeliveryStatus.PICKED_UP)
                || (currentStatus == DeliveryStatus.PICKED_UP && requestedStatus == DeliveryStatus.ON_THE_WAY)
                || (currentStatus == DeliveryStatus.ON_THE_WAY && requestedStatus == DeliveryStatus.DELIVERED);
    }

    private void applyStatusChange(DeliveryAssignment assignment, DeliveryStatus requestedStatus) {
        LocalDateTime now = LocalDateTime.now();
        assignment.setStatus(requestedStatus);

        if (requestedStatus == DeliveryStatus.PICKED_UP) {
            assignment.setPickedUpAt(now);
        }
        if (requestedStatus == DeliveryStatus.ON_THE_WAY) {
            assignment.getOrder().setStatus(OrderStatus.ON_THE_WAY);
        }
        if (requestedStatus == DeliveryStatus.DELIVERED) {
            assignment.setDeliveredAt(now);
            applyDeliveryEarningsSnapshot(assignment);
            if (orderService != null) {
                orderService.markDelivered(assignment.getOrder(), assignment.getDeliveryUser());
            } else {
                assignment.getOrder().setStatus(OrderStatus.DELIVERED);
            }
        }
    }

    private void applyDeliveryEarningsSnapshot(DeliveryAssignment assignment) {
        BigDecimal deliveryFee = nullToZero(assignment.getOrder().getDeliveryFee());
        BigDecimal tip = nullToZero(assignment.getOrder().getTipAmount());
        BigDecimal commissionPercentage = currentDeliveryCommissionPercentage();
        BigDecimal commissionAmount = deliveryFee
                .multiply(commissionPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal grossEarnings = deliveryFee.add(tip).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netEarnings = grossEarnings.subtract(commissionAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        assignment.setDeliveryGrossEarnings(grossEarnings);
        assignment.setDeliveryPlatformCommissionPercentage(commissionPercentage.setScale(2, RoundingMode.HALF_UP));
        assignment.setDeliveryPlatformCommissionAmount(commissionAmount);
        assignment.setDeliveryNetEarnings(netEarnings);
    }

    private BigDecimal currentDeliveryCommissionPercentage() {
        if (jdbcTemplate == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal percentage = jdbcTemplate.queryForObject("""
                select coalesce(pc.delivery_commission_percentage, 0)
                from platform_commissions pc
                where pc.starts_at <= now()
                  and (pc.ends_at is null or pc.ends_at > now())
                order by pc.starts_at desc
                limit 1
                """, BigDecimal.class);
        return percentage == null ? BigDecimal.ZERO : percentage;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private DeliveryAssignment findOwnedAssignmentForUpdate(UUID deliveryAssignmentId, UUID deliveryUserId) {
        DeliveryAssignment assignment = deliveryAssignmentRepository.findWithLockingById(deliveryAssignmentId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery assignment was not found"));
        if (!assignment.getDeliveryUser().getId().equals(deliveryUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Delivery assignment does not belong to the authenticated delivery user");
        }
        return assignment;
    }

    private Optional<User> findNextCandidate(UUID orderId) {
        return deliveryAssignmentRepository.findNearestCandidateForOrder(orderId)
                .or(() -> deliveryAssignmentRepository.findFirstCandidateForOrder(orderId));
    }

    private void registerRejection(UUID orderId, UUID deliveryUserId) {
        requireJdbc();
        jdbcTemplate.update("""
                insert into delivery_assignment_rejections (id, order_id, delivery_user_id, rejected_at)
                values (cast(? as uuid), cast(? as uuid), cast(? as uuid), now())
                on conflict (order_id, delivery_user_id) do nothing
                """, UUID.randomUUID(), orderId, deliveryUserId);
    }

    private DeliveryProfileResponse getProfile(User deliveryUser) {
        requireJdbc();
        return jdbcTemplate.query("""
                select coalesce(dp.is_available, true) as is_available,
                       ST_Y(latest_location.location::geometry) as latitude,
                       ST_X(latest_location.location::geometry) as longitude,
                       latest_location.recorded_at as recorded_at
                from users u
                left join delivery_profiles dp on dp.delivery_user_id = u.id
                left join lateral (
                    select dl.location, dl.recorded_at
                    from delivery_locations dl
                    where dl.delivery_user_id = u.id
                    order by dl.recorded_at desc
                    limit 1
                ) latest_location on true
                where u.id = cast(? as uuid)
                """, rs -> {
                    if (!rs.next()) {
                        return new DeliveryProfileResponse(
                                deliveryUser.getId(),
                                fullName(deliveryUser),
                                true,
                                null,
                                null,
                                null,
                                averageRating(deliveryUser.getId()),
                                reviewCount(deliveryUser.getId())
                        );
                    }
                    return new DeliveryProfileResponse(
                            deliveryUser.getId(),
                            fullName(deliveryUser),
                            rs.getBoolean("is_available"),
                            (Double) rs.getObject("latitude"),
                            (Double) rs.getObject("longitude"),
                            rs.getTimestamp("recorded_at") == null ? null : rs.getTimestamp("recorded_at").toLocalDateTime(),
                            averageRating(deliveryUser.getId()),
                            reviewCount(deliveryUser.getId())
                    );
                }, deliveryUser.getId());
    }

    private Double averageRating(UUID deliveryUserId) {
        return reviewRepository == null ? null : reviewRepository.averageRatingByDeliveryUserId(deliveryUserId);
    }

    private long reviewCount(UUID deliveryUserId) {
        return reviewRepository == null ? 0 : reviewRepository.countByDeliveryUserId(deliveryUserId);
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private void requireJdbc() {
        if (jdbcTemplate == null) {
            throw new DeliveryBusinessException("Delivery persistence support is not available in this test context");
        }
    }
}
