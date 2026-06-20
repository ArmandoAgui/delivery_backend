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
import com.paypal.sdk.models.PurchaseUnitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.payment.dto.PaypalCreateOrderRequest;
import sv.edu.uca.delivery.backend.payment.dto.PaypalOrderResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaypalPaymentService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final ObjectProvider<PaypalServerSdkClient> paypalClientProvider;

    public PaypalOrderResponse createOrder(PaypalCreateOrderRequest request) {
        BigDecimal amount = normalizeAmount(request.amount());
        String currency = normalizeCurrency(request.currency());
        String description = request.description() == null || request.description().isBlank()
                ? "Delivery order"
                : request.description().trim();

        try {
            Order order = ordersController().createOrder(new CreateOrderInput.Builder(
                    null,
                    new OrderRequest.Builder(
                            CheckoutPaymentIntent.fromString("CAPTURE"),
                            List.of(new PurchaseUnitRequest.Builder(
                                    new AmountWithBreakdown.Builder(currency, amount.toPlainString())
                                            .breakdown(new AmountBreakdown.Builder()
                                                    .itemTotal(new Money(currency, amount.toPlainString()))
                                                    .build())
                                            .build()
                            )
                                    .items(List.of(new ItemRequest.Builder(
                                            description,
                                            new Money.Builder(currency, amount.toPlainString()).build(),
                                            "1"
                                    )
                                            .description("Delivery checkout")
                                            .sku("delivery-checkout")
                                            .category(ItemCategory.PHYSICAL_GOODS)
                                            .build()))
                                    .build())
                    ).build()
            ).build()).getResult();
            return toResponse(order);
        } catch (IOException | ApiException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Could not create PayPal order");
        }
    }

    public PaypalOrderResponse captureOrder(String paypalOrderId) {
        if (paypalOrderId == null || paypalOrderId.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PayPal order id is required");
        }
        try {
            ApiResponse<Order> apiResponse = ordersController().captureOrder(new CaptureOrderInput.Builder(
                    paypalOrderId,
                    null
            ).build());
            return toResponse(apiResponse.getResult());
        } catch (IOException | ApiException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Could not capture PayPal order");
        }
    }

    private OrdersController ordersController() {
        PaypalServerSdkClient client = paypalClientProvider.getIfAvailable();
        if (client == null) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "PayPal payments are not configured");
        }
        return client.getOrdersController();
    }

    private PaypalOrderResponse toResponse(Order order) {
        return new PaypalOrderResponse(
                order.getId(),
                order.getStatus() == null ? null : order.getStatus().toString(),
                approvalUrl(order.getLinks())
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

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PayPal amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return DEFAULT_CURRENCY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
