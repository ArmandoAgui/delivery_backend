package sv.edu.uca.delivery.backend.report.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import sv.edu.uca.delivery.backend.order.entity.Order;

import java.math.BigDecimal;
import java.util.List;

public interface ReportRepository extends Repository<Order, java.util.UUID> {

    @Query(value = """
            select cast(r.id as text), r.name, count(o.id), coalesce(sum(o.total_amount), 0)
            from restaurants r
            left join orders o on o.restaurant_id = r.id
            group by r.id, r.name
            order by count(o.id) desc
            """, nativeQuery = true)
    List<Object[]> restaurantOrderStats();

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o")
    BigDecimal revenue();
}
