package sv.edu.uca.delivery.backend.complaint.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.uca.delivery.backend.auth.entity.Role;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
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
import sv.edu.uca.delivery.backend.loyalty.service.LoyaltyService;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.order.entity.OrderStatus;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.payment.entity.Payment;
import sv.edu.uca.delivery.backend.payment.entity.PaymentStatus;
import sv.edu.uca.delivery.backend.payment.repository.PaymentRepository;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("018f0000-0000-7000-8000-000000000003");
    private static final UUID ADMIN_ID = UUID.fromString("018f0000-0000-7000-8000-000000000001");
    private static final UUID ORDER_ID = UUID.fromString("018f0000-0000-7000-8000-000000000010");
    private static final UUID COMPLAINT_ID = UUID.fromString("018f0000-0000-7000-8000-000000000020");

    @Mock
    private ComplaintRepository complaintRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;
    @Mock
    private LoyaltyService loyaltyService;

    @InjectMocks
    private ComplaintService complaintService;

    @BeforeEach
    void setUp() {
        complaintService = new ComplaintService(
                complaintRepository,
                refundRepository,
                orderRepository,
                userRepository,
                paymentRepository,
                new ComplaintMapper(),
                authenticatedUserProvider,
                loyaltyService
        );
    }

    @Test
    void createComplaintForDeliveredOwnedOrder() {
        User customer = user(CUSTOMER_ID, RoleName.CUSTOMER);
        Order order = order(customer, OrderStatus.DELIVERED);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CUSTOMER_ID);
        when(userRepository.findByIdAndActiveTrueAndRoleName(CUSTOMER_ID, RoleName.CUSTOMER))
                .thenReturn(Optional.of(customer));
        when(orderRepository.findWithCustomerByIdForUpdate(ORDER_ID)).thenReturn(Optional.of(order));
        when(complaintRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint complaint = invocation.getArgument(0);
            complaint.setId(COMPLAINT_ID);
            return complaint;
        });

        var response = complaintService.createComplaint(
                new CreateComplaintRequest(ORDER_ID, " Missing item ", " The drink was missing ")
        );

        assertThat(response.id()).isEqualTo(COMPLAINT_ID);
        assertThat(response.status()).isEqualTo(ComplaintStatus.OPEN);
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    void createComplaintRejectsNonDeliveredOrder() {
        User customer = user(CUSTOMER_ID, RoleName.CUSTOMER);
        Order order = order(customer, OrderStatus.ON_THE_WAY);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CUSTOMER_ID);
        when(userRepository.findByIdAndActiveTrueAndRoleName(CUSTOMER_ID, RoleName.CUSTOMER))
                .thenReturn(Optional.of(customer));
        when(orderRepository.findWithCustomerByIdForUpdate(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> complaintService.createComplaint(
                new CreateComplaintRequest(ORDER_ID, "Late", "Still waiting")
        ))
                .isInstanceOf(ComplaintBusinessException.class)
                .hasMessage("Only delivered orders can generate complaints");
    }

    @Test
    void createComplaintRejectsDuplicateOrderComplaint() {
        User customer = user(CUSTOMER_ID, RoleName.CUSTOMER);
        Order order = order(customer, OrderStatus.DELIVERED);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CUSTOMER_ID);
        when(userRepository.findByIdAndActiveTrueAndRoleName(CUSTOMER_ID, RoleName.CUSTOMER))
                .thenReturn(Optional.of(customer));
        when(orderRepository.findWithCustomerByIdForUpdate(ORDER_ID)).thenReturn(Optional.of(order));
        when(complaintRepository.existsByOrderId(ORDER_ID)).thenReturn(true);

        assertThatThrownBy(() -> complaintService.createComplaint(
                new CreateComplaintRequest(ORDER_ID, "Wrong order", "Wrong bag")
        ))
                .isInstanceOf(ComplaintBusinessException.class)
                .hasMessage("Order already has a complaint");
    }

    @Test
    void updateStatusAllowsOpenToInProgress() {
        Complaint complaint = complaint(ComplaintStatus.OPEN);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(ADMIN_ID);
        when(userRepository.findByIdAndActiveTrueAndRoleName(ADMIN_ID, RoleName.ADMIN))
                .thenReturn(Optional.of(user(ADMIN_ID, RoleName.ADMIN)));
        when(complaintRepository.findWithOrderAndCustomerByIdForUpdate(COMPLAINT_ID))
                .thenReturn(Optional.of(complaint));
        when(complaintRepository.save(complaint)).thenReturn(complaint);

        var response = complaintService.updateStatus(
                COMPLAINT_ID,
                new UpdateComplaintStatusRequest(ComplaintStatus.IN_PROGRESS)
        );

        assertThat(response.status()).isEqualTo(ComplaintStatus.IN_PROGRESS);
    }

    @Test
    void updateStatusRejectsInvalidTransition() {
        Complaint complaint = complaint(ComplaintStatus.OPEN);

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(ADMIN_ID);
        when(userRepository.findByIdAndActiveTrueAndRoleName(ADMIN_ID, RoleName.ADMIN))
                .thenReturn(Optional.of(user(ADMIN_ID, RoleName.ADMIN)));
        when(complaintRepository.findWithOrderAndCustomerByIdForUpdate(COMPLAINT_ID))
                .thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> complaintService.updateStatus(
                COMPLAINT_ID,
                new UpdateComplaintStatusRequest(ComplaintStatus.RESOLVED)
        ))
                .isInstanceOf(ComplaintBusinessException.class)
                .hasMessage("Invalid complaint status transition");
    }

    @Test
    void updateStatusApprovesSimpleRefundWhenResolved() {
        Complaint complaint = complaint(ComplaintStatus.IN_PROGRESS);
        Payment payment = payment(complaint.getOrder());

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(ADMIN_ID);
        when(userRepository.findByIdAndActiveTrueAndRoleName(ADMIN_ID, RoleName.ADMIN))
                .thenReturn(Optional.of(user(ADMIN_ID, RoleName.ADMIN)));
        when(complaintRepository.findWithOrderAndCustomerByIdForUpdate(COMPLAINT_ID))
                .thenReturn(Optional.of(complaint));
        when(refundRepository.existsByComplaintId(COMPLAINT_ID)).thenReturn(false);
        when(paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(ORDER_ID, PaymentStatus.PAID))
                .thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund refund = invocation.getArgument(0);
            refund.setId(UUID.fromString("018f0000-0000-7000-8000-000000000030"));
            return refund;
        });
        when(complaintRepository.save(complaint)).thenReturn(complaint);

        var response = complaintService.updateStatus(
                COMPLAINT_ID,
                new UpdateComplaintStatusRequest(ComplaintStatus.RESOLVED)
        );

        assertThat(response.status()).isEqualTo(ComplaintStatus.RESOLVED);
        assertThat(response.refund()).isNotNull();
        assertThat(response.refund().refundRequested()).isTrue();
        assertThat(response.refund().refundStatus()).isEqualTo(RefundStatus.APPROVED);
        verify(loyaltyService).creditRefund(
                complaint.getCustomer(),
                complaint.getOrder(),
                BigDecimal.valueOf(25),
                "Total refund credited to digital wallet"
        );
    }

    @Test
    void getComplaintFailsWhenMissing() {
        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(CUSTOMER_ID);
        when(userRepository.findByIdAndActiveTrue(CUSTOMER_ID)).thenReturn(Optional.of(user(CUSTOMER_ID, RoleName.CUSTOMER)));
        when(complaintRepository.findDetailById(COMPLAINT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> complaintService.getComplaint(COMPLAINT_ID))
                .isInstanceOf(ComplaintNotFoundException.class)
                .hasMessage("Complaint was not found");
    }

    private Complaint complaint(ComplaintStatus status) {
        User customer = user(CUSTOMER_ID, RoleName.CUSTOMER);
        Complaint complaint = new Complaint();
        complaint.setId(COMPLAINT_ID);
        complaint.setOrder(order(customer, OrderStatus.DELIVERED));
        complaint.setCustomer(customer);
        complaint.setStatus(status);
        complaint.setSubject("Missing item");
        complaint.setDescription("A product was missing");
        return complaint;
    }

    private Order order(User customer, OrderStatus status) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setCustomer(customer);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(25));
        return order;
    }

    private Payment payment(Order order) {
        Payment payment = new Payment();
        payment.setId(UUID.fromString("018f0000-0000-7000-8000-000000000040"));
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.PAID);
        payment.setAmount(BigDecimal.valueOf(25));
        payment.setCurrency("USD");
        return payment;
    }

    private User user(UUID id, RoleName roleName) {
        Role role = new Role();
        role.setId(roleName == RoleName.ADMIN ? 1L : 2L);
        role.setName(roleName);

        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(id + "@example.test");
        user.setPasswordHash("secret");
        user.setActive(true);
        return user;
    }
}
