package sv.edu.uca.delivery.backend.dto;

import java.math.BigDecimal;

public record AdminSummaryResponse(
        long users,
        long restaurants,
        long orders,
        BigDecimal revenue,
        long openComplaints,
        BigDecimal estimatedCommissions
) {
}
