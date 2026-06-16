package sv.edu.uca.delivery.backend.complaint.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.complaint.dto.ComplaintResponse;
import sv.edu.uca.delivery.backend.complaint.dto.CreateComplaintRequest;
import sv.edu.uca.delivery.backend.complaint.dto.UpdateComplaintStatusRequest;
import sv.edu.uca.delivery.backend.complaint.entity.Complaint;
import sv.edu.uca.delivery.backend.complaint.entity.ComplaintStatus;
import sv.edu.uca.delivery.backend.complaint.entity.Refund;
import sv.edu.uca.delivery.backend.complaint.entity.RefundStatus;
import sv.edu.uca.delivery.backend.complaint.exception.ComplaintBusinessException;
import sv.edu.uca.delivery.backend.complaint.exception.ComplaintNotFoundException;
import sv.edu.uca.delivery.backend.complaint.mapper.ComplaintMapper;
import sv.edu.uca.delivery.backend.complaint.repository.ComplaintRepository;
import sv.edu.uca.delivery.backend.complaint.repository.RefundRepository;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.payment.entity.Payment;
import sv.edu.uca.delivery.backend.payment.entity.PaymentStatus;
import sv.edu.uca.delivery.backend.payment.repository.PaymentRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ComplaintMapper complaintMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request) {
        UUID customerUserId = authenticatedUserProvider.getCurrentUserId();
        User customer = validateCustomer(customerUserId);

        Order order = orderRepository.findWithCustomerByIdForUpdate(request.orderId())
                .orElseThrow(() -> new ComplaintNotFoundException("Order was not found"));

        validateComplaintCanBeCreated(order, customer);

        Complaint complaint = new Complaint();
        complaint.setOrder(order);
        complaint.setCustomer(customer);
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setSubject(request.subject().trim());
        complaint.setDescription(request.description().trim());

        Complaint savedComplaint = complaintRepository.save(complaint);
        return complaintMapper.toResponse(savedComplaint, Optional.empty());
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> listComplaints(ComplaintStatus status, UUID orderId) {
        UUID customerUserId = authenticatedUserProvider.getCurrentUserId();
        User currentUser = validateActiveUser(customerUserId);
        UUID customerFilter = currentUser.getRole().getName() == RoleName.ADMIN ? null : customerUserId;

        return complaintRepository.findAllFiltered(customerFilter, status, orderId)
                .stream()
                .map(complaint -> complaintMapper.toResponse(
                        complaint,
                        refundRepository.findFirstByComplaintIdOrderByCreatedAtDesc(complaint.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaint(UUID complaintId) {
        UUID currentUserId = authenticatedUserProvider.getCurrentUserId();
        User currentUser = validateActiveUser(currentUserId);

        Complaint complaint = complaintRepository.findDetailById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint was not found"));
        validateCanViewComplaint(complaint, currentUser);

        return complaintMapper.toResponse(
                complaint,
                refundRepository.findFirstByComplaintIdOrderByCreatedAtDesc(complaint.getId())
        );
    }

    @Transactional
    public ComplaintResponse updateStatus(UUID complaintId, UpdateComplaintStatusRequest request) {
        UUID currentUserId = authenticatedUserProvider.getCurrentUserId();
        validateAdmin(currentUserId);

        Complaint complaint = complaintRepository.findWithOrderAndCustomerByIdForUpdate(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint was not found"));

        validateStatusChange(complaint.getStatus(), request.status());
        complaint.setStatus(request.status());

        Optional<Refund> refund = Optional.empty();
        if (request.status() == ComplaintStatus.RESOLVED) {
            refund = Optional.of(approveSimpleRefund(complaint));
            complaint.setResolution("Complaint resolved and refund approved");
        }
        if (request.status() == ComplaintStatus.REJECTED) {
            complaint.setResolution("Complaint rejected");
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        return complaintMapper.toResponse(savedComplaint, refund);
    }

    private User validateCustomer(UUID customerUserId) {
        return userRepository.findByIdAndActiveTrueAndRoleName(customerUserId, RoleName.CUSTOMER)
                .orElseThrow(() -> new ComplaintBusinessException("Customer user must exist and be active"));
    }

    private User validateActiveUser(UUID userId) {
        User user = userRepository.findByIdWithRole(userId)
                .or(() -> userRepository.findByIdAndActiveTrue(userId))
                .orElseThrow(() -> new ComplaintBusinessException("Authenticated user must exist and be active"));
        if (!user.isActive()) {
            throw new ComplaintBusinessException("Authenticated user must exist and be active");
        }
        return user;
    }

    private User validateAdmin(UUID userId) {
        return userRepository.findByIdAndActiveTrueAndRoleName(userId, RoleName.ADMIN)
                .orElseThrow(() -> new ComplaintBusinessException("Only an active admin can update complaint status"));
    }

    private void validateComplaintCanBeCreated(Order order, User customer) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ComplaintBusinessException("Only delivered orders can generate complaints");
        }
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ComplaintBusinessException("Only the owner of the order can create a complaint");
        }
        if (complaintRepository.existsByOrderId(order.getId())) {
            throw new ComplaintBusinessException("Order already has a complaint");
        }
    }

    private void validateCanViewComplaint(Complaint complaint, User currentUser) {
        if (currentUser.getRole().getName() == RoleName.ADMIN) {
            return;
        }
        if (!complaint.getCustomer().getId().equals(currentUser.getId())) {
            throw new ComplaintBusinessException("Complaint does not belong to the authenticated user");
        }
    }

    private void validateStatusChange(ComplaintStatus currentStatus, ComplaintStatus requestedStatus) {
        if (currentStatus == ComplaintStatus.RESOLVED || currentStatus == ComplaintStatus.REJECTED) {
            throw new ComplaintBusinessException("Resolved or rejected complaints cannot be modified");
        }
        if (!isValidTransition(currentStatus, requestedStatus)) {
            throw new ComplaintBusinessException("Invalid complaint status transition");
        }
    }

    private boolean isValidTransition(ComplaintStatus currentStatus, ComplaintStatus requestedStatus) {
        return (currentStatus == ComplaintStatus.OPEN && requestedStatus == ComplaintStatus.IN_PROGRESS)
                || (currentStatus == ComplaintStatus.IN_PROGRESS && requestedStatus == ComplaintStatus.RESOLVED)
                || (currentStatus == ComplaintStatus.IN_PROGRESS && requestedStatus == ComplaintStatus.REJECTED);
    }

    private Refund approveSimpleRefund(Complaint complaint) {
        if (refundRepository.existsByComplaintId(complaint.getId())) {
            throw new ComplaintBusinessException("Complaint already has a refund");
        }

        Payment payment = paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
                        complaint.getOrder().getId(),
                        PaymentStatus.PAID
                )
                .orElseThrow(() -> new ComplaintBusinessException("Paid payment was not found for the order"));

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setComplaint(complaint);
        refund.setStatus(RefundStatus.APPROVED);
        refund.setAmount(payment.getAmount());
        refund.setReason("Academic simple refund for resolved complaint");
        refund.setProcessedAt(LocalDateTime.now());

        return refundRepository.save(refund);
    }
}
