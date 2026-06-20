package sv.edu.uca.delivery.backend.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaypalCheckoutRequest(
        @NotNull UUID deliveryAddressId,
        @DecimalMin("0.0") BigDecimal tipAmount,
        String couponCode,
        String notes,
        Boolean useLoyaltyPoints
) {
}
