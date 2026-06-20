package sv.edu.uca.delivery.backend.payment.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record PaypalCreateOrderRequest(
        @DecimalMin("0.01") BigDecimal amount,
        String currency,
        String description
) {
}
