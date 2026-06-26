package sv.edu.uca.delivery.backend.dto;

public record RoleCountReportResponse(
        String role,
        long users
) {
}
