package sv.edu.uca.delivery.backend.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.report.dto.AdminSummaryResponse;
import sv.edu.uca.delivery.backend.report.dto.RestaurantOrdersReportResponse;
import sv.edu.uca.delivery.backend.report.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reportes administrativos y metricas operativas del negocio.")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/restaurants/most-ordered")
    @Operation(summary = "Restaurantes mas pedidos", description = "Lista restaurantes ordenados por volumen de pedidos.")
    public List<RestaurantOrdersReportResponse> restaurantsMostOrdered() {
        return reportService.restaurantsMostOrdered();
    }

    @GetMapping("/admin-summary")
    @Operation(summary = "Resumen administrativo", description = "Obtiene metricas generales para administracion.")
    public AdminSummaryResponse adminSummary() {
        return reportService.adminSummary();
    }
}
