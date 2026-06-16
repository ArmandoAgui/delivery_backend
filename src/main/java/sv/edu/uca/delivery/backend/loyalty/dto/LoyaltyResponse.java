package sv.edu.uca.delivery.backend.loyalty.dto;

import java.util.List;
import java.util.UUID;

public record LoyaltyResponse(
        UUID customerId,
        Integer pointsBalance,
        List<LoyaltyTransactionResponse> transactions
) {
}
