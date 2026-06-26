package sv.edu.uca.delivery.backend.dto;

import java.math.BigDecimal;

public record StatusCountReportResponse(
        String status,
        long count,
        BigDecimal amount
) {
}
