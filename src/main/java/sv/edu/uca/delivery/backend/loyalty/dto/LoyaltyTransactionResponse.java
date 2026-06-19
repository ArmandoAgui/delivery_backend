package sv.edu.uca.delivery.backend.loyalty.dto;

import sv.edu.uca.delivery.backend.loyalty.entity.LoyaltyTransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoyaltyTransactionResponse(
        UUID id,
        LoyaltyTransactionType type,
        Integer points,
        String description,
        LocalDateTime createdAt
) {
}
