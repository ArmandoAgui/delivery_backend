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

    @Query(value = """
            select cast(r.id as text),
                   r.name,
                   count(o.id),
                   coalesce(sum(o.total_amount), 0) as revenue,
                   coalesce(latest_commission.commission_percentage, 0) as commission_percentage,
                   coalesce(sum(o.total_amount), 0) * coalesce(latest_commission.commission_percentage, 0) / 100 as commission_amount
            from restaurants r
            left join orders o on o.restaurant_id = r.id
            left join lateral (
                select pc.commission_percentage
                from platform_commissions pc
                where pc.starts_at <= now()
                  and (pc.ends_at is null or pc.ends_at > now())
                order by pc.starts_at desc
                limit 1
            ) latest_commission on true
            group by r.id, r.name, latest_commission.commission_percentage
            order by commission_amount desc, revenue desc
            """, nativeQuery = true)
    List<Object[]> restaurantCommissionStats();

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o")
    BigDecimal revenue();

    @Query(value = """
            select o.status, count(o.id), coalesce(sum(o.total_amount), 0)
            from orders o
            group by o.status
            order by count(o.id) desc
            """, nativeQuery = true)
    List<Object[]> ordersByStatus();

    @Query(value = """
            select c.status, count(c.id), 0
            from complaints c
            group by c.status
            order by count(c.id) desc
            """, nativeQuery = true)
    List<Object[]> complaintsByStatus();

    @Query(value = """
            select r.name, count(u.id)
            from roles r
            left join users u on u.role_id = r.id
            group by r.name
            order by r.name
            """, nativeQuery = true)
    List<Object[]> usersByRole();

    @Query(value = """
            select cast(u.id as text),
                   concat(u.first_name, ' ', u.last_name),
                   count(da.id),
                   coalesce(sum(o.delivery_fee + o.tip_amount), 0)
            from users u
            join roles role on role.id = u.role_id and role.name = 'DELIVERY'
            left join delivery_assignments da on da.delivery_user_id = u.id and da.status = 'DELIVERED'
            left join orders o on o.id = da.order_id
            group by u.id, u.first_name, u.last_name
            order by count(da.id) desc
            limit 10
            """, nativeQuery = true)
    List<Object[]> topDeliveryUsers();

    @Query(value = """
            select cast(oi.product_id as text),
                   oi.product_name,
                   r.name,
                   coalesce(sum(oi.quantity), 0),
                   coalesce(sum(oi.line_total), 0)
            from order_items oi
            join orders o on o.id = oi.order_id
            join restaurants r on r.id = o.restaurant_id
            group by oi.product_id, oi.product_name, r.name
            order by coalesce(sum(oi.quantity), 0) desc
            limit 10
            """, nativeQuery = true)
    List<Object[]> topProducts();

    @Query(value = "select count(*) from complaints where status in ('OPEN', 'IN_PROGRESS')", nativeQuery = true)
    long openComplaints();

    @Query(value = """
            select coalesce(sum(o.total_amount), 0) * coalesce(latest_commission.commission_percentage, 0) / 100
            from orders o
            left join lateral (
                select pc.commission_percentage
                from platform_commissions pc
                where pc.starts_at <= now()
                  and (pc.ends_at is null or pc.ends_at > now())
                order by pc.starts_at desc
                limit 1
            ) latest_commission on true
            group by latest_commission.commission_percentage
            """, nativeQuery = true)
    BigDecimal estimatedCommissions();
}
