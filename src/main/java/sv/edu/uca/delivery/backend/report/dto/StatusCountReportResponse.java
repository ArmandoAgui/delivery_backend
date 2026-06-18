package sv.edu.uca.delivery.backend.report.dto;

import java.math.BigDecimal;

public record StatusCountReportResponse(
        String status,
        long count,
        BigDecimal amount
) {
}
