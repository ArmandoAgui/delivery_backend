package sv.edu.uca.delivery.backend.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.admin.dto.CommissionRequest;
import sv.edu.uca.delivery.backend.admin.dto.CommissionResponse;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public CommissionResponse createCommission(CommissionRequest request) {
        return jdbcTemplate.queryForObject("""
                        insert into restaurant_commissions (restaurant_id, commission_percentage, starts_at, ends_at)
                        values (cast(? as uuid), ?, ?, ?)
                        returning id, restaurant_id, commission_percentage, starts_at, ends_at
                        """,
                (rs, rowNum) -> new CommissionResponse(
                        rs.getLong("id"),
                        rs.getObject("restaurant_id", UUID.class),
                        rs.getBigDecimal("commission_percentage"),
                        rs.getTimestamp("starts_at").toLocalDateTime(),
                        rs.getTimestamp("ends_at") == null ? null : rs.getTimestamp("ends_at").toLocalDateTime()
                ),
                request.restaurantId(),
                request.commissionPercentage(),
                Timestamp.valueOf(request.startsAt()),
                request.endsAt() == null ? null : Timestamp.valueOf(request.endsAt()));
    }

    @Transactional(readOnly = true)
    public List<CommissionResponse> listCommissions() {
        return jdbcTemplate.query("""
                        select id, restaurant_id, commission_percentage, starts_at, ends_at
                        from restaurant_commissions
                        order by starts_at desc
                        """,
                (rs, rowNum) -> new CommissionResponse(
                        rs.getLong("id"),
                        rs.getObject("restaurant_id", UUID.class),
                        rs.getBigDecimal("commission_percentage"),
                        rs.getTimestamp("starts_at").toLocalDateTime(),
                        rs.getTimestamp("ends_at") == null ? null : rs.getTimestamp("ends_at").toLocalDateTime()
                ));
    }
}
