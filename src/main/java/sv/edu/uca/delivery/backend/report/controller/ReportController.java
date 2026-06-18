package sv.edu.uca.delivery.backend.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.report.dto.AdminSummaryResponse;
import sv.edu.uca.delivery.backend.report.dto.RoleCountReportResponse;
import sv.edu.uca.delivery.backend.report.dto.RestaurantOrdersReportResponse;
import sv.edu.uca.delivery.backend.report.dto.RestaurantCommissionReportResponse;
import sv.edu.uca.delivery.backend.report.dto.StatusCountReportResponse;
import sv.edu.uca.delivery.backend.report.dto.TopDeliveryReportResponse;
import sv.edu.uca.delivery.backend.report.dto.TopProductReportResponse;
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

    @GetMapping("/restaurants/commissions")
    @Operation(summary = "Comisiones por restaurante", description = "Calcula ingreso por comision usando el porcentaje global vigente.")
    public List<RestaurantCommissionReportResponse> restaurantCommissions() {
        return reportService.restaurantCommissions();
    }

    @GetMapping("/admin-summary")
    @Operation(summary = "Resumen administrativo", description = "Obtiene metricas generales para administracion.")
    public AdminSummaryResponse adminSummary() {
        return reportService.adminSummary();
    }

    @GetMapping("/orders/by-status")
    @Operation(summary = "Pedidos por estado", description = "Agrupa pedidos por estado con conteo y monto acumulado.")
    public List<StatusCountReportResponse> ordersByStatus() {
        return reportService.ordersByStatus();
    }

    @GetMapping("/complaints/by-status")
    @Operation(summary = "Reclamos por estado", description = "Agrupa reclamos por estado.")
    public List<StatusCountReportResponse> complaintsByStatus() {
        return reportService.complaintsByStatus();
    }

    @GetMapping("/users/by-role")
    @Operation(summary = "Usuarios por rol", description = "Agrupa usuarios registrados por rol.")
    public List<RoleCountReportResponse> usersByRole() {
        return reportService.usersByRole();
    }

    @GetMapping("/deliveries/top")
    @Operation(summary = "Top repartidores", description = "Lista repartidores con mas entregas completadas.")
    public List<TopDeliveryReportResponse> topDeliveryUsers() {
        return reportService.topDeliveryUsers();
    }

    @GetMapping("/products/top")
    @Operation(summary = "Top productos", description = "Lista productos mas vendidos por cantidad.")
    public List<TopProductReportResponse> topProducts() {
        return reportService.topProducts();
    }
}
