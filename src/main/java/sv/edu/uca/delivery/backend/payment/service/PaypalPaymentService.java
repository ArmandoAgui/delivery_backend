package sv.edu.uca.delivery.backend.payment.service;

import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.AmountBreakdown;
import com.paypal.sdk.models.AmountWithBreakdown;
import com.paypal.sdk.models.CaptureOrderInput;
import com.paypal.sdk.models.CheckoutPaymentIntent;
import com.paypal.sdk.models.CreateOrderInput;
import com.paypal.sdk.models.ItemCategory;
import com.paypal.sdk.models.ItemRequest;
import com.paypal.sdk.models.LinkDescription;
import com.paypal.sdk.models.Money;
import com.paypal.sdk.models.Order;
import com.paypal.sdk.models.OrderRequest;
import com.paypal.sdk.models.OrdersCapture;
import com.paypal.sdk.models.PaymentCollection;
import com.paypal.sdk.models.PurchaseUnit;
import com.paypal.sdk.models.PurchaseUnitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.common.time.AppClock;
import sv.edu.uca.delivery.backend.order.dto.request.CreateOrderFromCartRequest;
import sv.edu.uca.delivery.backend.order.dto.response.OrderResponse;
import sv.edu.uca.delivery.backend.order.service.OrderService;
import sv.edu.uca.delivery.backend.payment.dto.PaypalCheckoutRequest;
import sv.edu.uca.delivery.backend.payment.dto.PaypalOrderResponse;
import sv.edu.uca.delivery.backend.payment.entity.Payment;
import sv.edu.uca.delivery.backend.payment.entity.PaymentStatus;
import sv.edu.uca.delivery.backend.payment.repository.PaymentRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaypalPaymentService {

    private static final String CURRENCY = "USD";
    private static final String PROVIDER = "PAYPAL";

    private final ObjectProvider<PaypalServerSdkClient> paypalClientProvider;
    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaypalOrderResponse createOrder(PaypalCheckoutRequest request) {
        sv.edu.uca.delivery.backend.order.entity.Order pendingOrder = orderService.createPendingPaymentOrderFromCart(toOrderRequest(request));
        BigDecimal amount = normalizeAmount(pendingOrder.getTotalAmount());
        String description = "Delivery order " + pendingOrder.getId();

        try {
            Order paypalOrder = ordersController().createOrder(new CreateOrderInput.Builder(
                    null,
                    new OrderRequest.Builder(
                            CheckoutPaymentIntent.fromString("CAPTURE"),
                            List.of(new PurchaseUnitRequest.Builder(
                                    new AmountWithBreakdown.Builder(CURRENCY, amount.toPlainString())
                                            .breakdown(new AmountBreakdown.Builder()
                                                    .itemTotal(new Money(CURRENCY, amount.toPlainString()))
                                                    .build())
                                            .build()
                            )
                                    .description(description)
                                    .customId(pendingOrder.getId().toString())
                                    .invoiceId(pendingOrder.getId().toString())
                                    .items(List.of(new ItemRequest.Builder(
                                            description,
                                            new Money.Builder(CURRENCY, amount.toPlainString()).build(),
                                            "1"
                                    )
                                            .description("Delivery checkout")
                                            .sku("delivery-order")
                                            .category(ItemCategory.PHYSICAL_GOODS)
                                            .build()))
                                    .build())
                    ).build()
            ).build()).getResult();

            Payment payment = new Payment();
            payment.setOrder(pendingOrder);
            payment.setProvider(PROVIDER);
            payment.setProviderTransactionId(paypalOrder.getId());
            payment.setPaypalOrderId(paypalOrder.getId());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(amount);
            payment.setCurrency(CURRENCY);
            paymentRepository.save(payment);

            return toResponse(paypalOrder, pendingOrder.getId(), amount, null);
        } catch (IOException | ApiException exception) {
            orderService.cancelPendingPaymentOrder(pendingOrder, "PayPal order creation failed");
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Could not create PayPal order");
        }
    }

    @Transactional
    public PaypalOrderResponse captureOrder(String paypalOrderId) {
        if (paypalOrderId == null || paypalOrderId.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PayPal order id is required");
        }
        Payment payment = paymentRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "PayPal payment was not found"));
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BusinessException(HttpStatus.CONFLICT, "PayPal payment was already captured");
        }

        try {
            ApiResponse<Order> apiResponse = ordersController().captureOrder(new CaptureOrderInput.Builder(
                    paypalOrderId,
                    null
            ).build());
            Order paypalOrder = apiResponse.getResult();
            if (paypalOrder.getStatus() != com.paypal.sdk.models.OrderStatus.COMPLETED) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new BusinessException(HttpStatus.CONFLICT, "PayPal payment was not completed");
            }
            String captureId = captureId(paypalOrder);
            payment.setPaypalCaptureId(captureId);
            payment.setPaidAt(AppClock.now());
            OrderResponse orderResponse = orderService.markPaypalPaymentPaid(payment);
            return toResponse(paypalOrder, payment.getOrder().getId(), payment.getAmount(), orderResponse);
        } catch (IOException | ApiException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Could not capture PayPal order");
        }
    }

    private CreateOrderFromCartRequest toOrderRequest(PaypalCheckoutRequest request) {
        return new CreateOrderFromCartRequest(
                request.deliveryAddressId(),
                request.tipAmount(),
                request.couponCode(),
                request.notes(),
                request.useLoyaltyPoints()
        );
    }

    private OrdersController ordersController() {
        PaypalServerSdkClient client = paypalClientProvider.getIfAvailable();
        if (client == null) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "PayPal payments are not configured");
        }
        return client.getOrdersController();
    }

    private PaypalOrderResponse toResponse(Order order, java.util.UUID internalOrderId, BigDecimal amount, OrderResponse orderResponse) {
        return new PaypalOrderResponse(
                order.getId(),
                internalOrderId,
                order.getStatus() == null ? null : order.getStatus().toString(),
                approvalUrl(order.getLinks()),
                amount,
                CURRENCY,
                orderResponse
        );
    }

    private String approvalUrl(List<LinkDescription> links) {
        if (links == null) {
            return null;
        }
        return links.stream()
                .filter(link -> "approve".equalsIgnoreCase(link.getRel()))
                .map(LinkDescription::getHref)
                .findFirst()
                .orElse(null);
    }

    private String captureId(Order order) {
        if (order.getPurchaseUnits() == null) {
            return null;
        }
        return order.getPurchaseUnits().stream()
                .map(PurchaseUnit::getPayments)
                .filter(payments -> payments != null && payments.getCaptures() != null)
                .map(PaymentCollection::getCaptures)
                .flatMap(List::stream)
                .filter(capture -> capture.getStatus() == com.paypal.sdk.models.CaptureStatus.COMPLETED)
                .map(OrdersCapture::getId)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PayPal amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
