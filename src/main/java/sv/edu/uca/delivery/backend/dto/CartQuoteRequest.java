package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.UUID;

public record CartQuoteRequest(
        UUID deliveryAddressId,
        @DecimalMin("0.0") BigDecimal tipAmount,
        String couponCode,
        Boolean useDigitalWallet
) {
}
