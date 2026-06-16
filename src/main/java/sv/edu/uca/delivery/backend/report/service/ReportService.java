package sv.edu.uca.delivery.backend.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.order.repository.OrderRepository;
import sv.edu.uca.delivery.backend.report.dto.AdminSummaryResponse;
import sv.edu.uca.delivery.backend.report.dto.RestaurantOrdersReportResponse;
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
    public AdminSummaryResponse adminSummary() {
        return new AdminSummaryResponse(
                userRepository.count(),
                restaurantRepository.count(),
                orderRepository.count(),
                reportRepository.revenue()
        );
    }
}
