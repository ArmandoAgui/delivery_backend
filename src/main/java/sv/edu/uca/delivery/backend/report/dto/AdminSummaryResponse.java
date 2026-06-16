package sv.edu.uca.delivery.backend.report.dto;

import java.math.BigDecimal;

public record AdminSummaryResponse(
        long users,
        long restaurants,
        long orders,
        BigDecimal revenue
) {
}
