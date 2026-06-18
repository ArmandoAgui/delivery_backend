package sv.edu.uca.delivery.backend.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.report.dto.AdminSummaryResponse;
import sv.edu.uca.delivery.backend.report.dto.RoleCountReportResponse;
import sv.edu.uca.delivery.backend.report.dto.RestaurantOrdersReportResponse;
import sv.edu.uca.delivery.backend.report.dto.RestaurantCommissionReportResponse;
import sv.edu.uca.delivery.backend.report.dto.StatusCountReportResponse;
import sv.edu.uca.delivery.backend.report.dto.TopDeliveryReportResponse;
import sv.edu.uca.delivery.backend.report.dto.TopProductReportResponse;
import sv.edu.uca.delivery.backend.report.repository.ReportRepository;
import sv.edu.uca.delivery.backend.restaurant.repository.RestaurantRepository;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<RestaurantOrdersReportResponse> restaurantsMostOrdered() {
        return reportRepository.restaurantOrderStats()
                .stream()
                .map(row -> new RestaurantOrdersReportResponse(
                        UUID.fromString((String) row[0]),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantCommissionReportResponse> restaurantCommissions() {
        return reportRepository.restaurantCommissionStats()
                .stream()
                .map(row -> new RestaurantCommissionReportResponse(
                        UUID.fromString((String) row[0]),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3],
                        (BigDecimal) row[4],
                        (BigDecimal) row[5]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminSummaryResponse adminSummary() {
        return new AdminSummaryResponse(
                userRepository.count(),
                restaurantRepository.count(),
                orderRepository.count(),
                reportRepository.revenue(),
                reportRepository.openComplaints(),
                reportRepository.estimatedCommissions()
        );
    }

    @Transactional(readOnly = true)
    public List<StatusCountReportResponse> ordersByStatus() {
        return reportRepository.ordersByStatus()
                .stream()
                .map(row -> new StatusCountReportResponse((String) row[0], ((Number) row[1]).longValue(), (BigDecimal) row[2]))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StatusCountReportResponse> complaintsByStatus() {
        return reportRepository.complaintsByStatus()
                .stream()
                .map(row -> new StatusCountReportResponse((String) row[0], ((Number) row[1]).longValue(), BigDecimal.ZERO))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleCountReportResponse> usersByRole() {
        return reportRepository.usersByRole()
                .stream()
                .map(row -> new RoleCountReportResponse((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopDeliveryReportResponse> topDeliveryUsers() {
        return reportRepository.topDeliveryUsers()
                .stream()
                .map(row -> new TopDeliveryReportResponse(
                        UUID.fromString((String) row[0]),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopProductReportResponse> topProducts() {
        return reportRepository.topProducts()
                .stream()
                .map(row -> new TopProductReportResponse(
                        UUID.fromString((String) row[0]),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue(),
                        (BigDecimal) row[4]
                ))
                .toList();
    }
}
