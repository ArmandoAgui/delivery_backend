package sv.edu.uca.delivery.backend.dto;

import sv.edu.uca.delivery.backend.entity.LoyaltyTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LoyaltyTransactionResponse(
        UUID id,
        LoyaltyTransactionType type,
        Integer points,
        BigDecimal creditAmount,
        String description,
        LocalDateTime createdAt
) {
}
