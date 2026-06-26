package sv.edu.uca.delivery.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.entity.Order;
import sv.edu.uca.delivery.backend.util.AppClock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RestaurantCommissionService {

    private final JdbcTemplate jdbcTemplate;

    public RestaurantCommissionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Snapshot snapshotFor(Order order) {
        BigDecimal grossAmount = nullToZero(order.getSubtotalAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal percentage = commissionPercentageAt(order.getCreatedAt() == null ? AppClock.now() : order.getCreatedAt());
        BigDecimal commissionAmount = grossAmount
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = grossAmount.subtract(commissionAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new Snapshot(
                grossAmount,
                percentage.setScale(2, RoundingMode.HALF_UP),
                commissionAmount,
                netAmount
        );
    }

    private BigDecimal commissionPercentageAt(LocalDateTime dateTime) {
        List<BigDecimal> values = jdbcTemplate.query("""
                select coalesce(pc.commission_percentage, 0)
                from platform_commissions pc
                where pc.starts_at <= ?
                  and (pc.ends_at is null or pc.ends_at > ?)
                order by pc.starts_at desc
                limit 1
                """,
                (rs, rowNum) -> rs.getBigDecimal(1),
                Timestamp.valueOf(dateTime),
                Timestamp.valueOf(dateTime));
        return values.isEmpty() || values.getFirst() == null ? BigDecimal.ZERO : values.getFirst();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record Snapshot(
            BigDecimal grossAmount,
            BigDecimal commissionPercentage,
            BigDecimal commissionAmount,
            BigDecimal netAmount
    ) {
    }
}
