package sv.edu.uca.delivery.backend.report.controller;

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
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/restaurants/most-ordered")
    public List<RestaurantOrdersReportResponse> restaurantsMostOrdered() {
        return reportService.restaurantsMostOrdered();
    }

    @GetMapping("/admin-summary")
    public AdminSummaryResponse adminSummary() {
        return reportService.adminSummary();
    }
}
