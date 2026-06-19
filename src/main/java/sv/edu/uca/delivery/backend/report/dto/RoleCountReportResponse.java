package sv.edu.uca.delivery.backend.report.dto;

public record RoleCountReportResponse(
        String role,
        long users
) {
}
