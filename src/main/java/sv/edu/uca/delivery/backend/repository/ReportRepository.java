package sv.edu.uca.delivery.backend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import sv.edu.uca.delivery.backend.entity.Order;

import java.math.BigDecimal;
import java.util.List;

public interface ReportRepository extends Repository<Order, java.util.UUID> {

    @Query(value = """
            select cast(r.id as text), r.name, count(o.id), coalesce(sum(o.subtotal_amount), 0)
            from restaurants r
            left join orders o on o.restaurant_id = r.id
                and o.status in ('CONFIRMED', 'WAITING_FOR_DRIVER', 'NO_DRIVER_AVAILABLE', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED')
            group by r.id, r.name
            order by count(o.id) desc
            """, nativeQuery = true)
    List<Object[]> restaurantOrderStats();

    @Query(value = """
            select cast(r.id as text),
                   r.name,
                   count(o.id),
                   coalesce(sum(o.subtotal_amount), 0) as revenue,
                   case
                       when coalesce(sum(o.subtotal_amount), 0) > 0 then
                           coalesce(sum(o.subtotal_amount * coalesce(applied_commission.commission_percentage, 0) / 100), 0)
                           / coalesce(sum(o.subtotal_amount), 0) * 100
                       else 0
                   end as effective_commission_percentage,
                   coalesce(sum(o.subtotal_amount * coalesce(applied_commission.commission_percentage, 0) / 100), 0) as commission_amount,
                   coalesce(sum(o.subtotal_amount), 0)
                       - coalesce(sum(o.subtotal_amount * coalesce(applied_commission.commission_percentage, 0) / 100), 0) as net_revenue
            from restaurants r
            left join orders o on o.restaurant_id = r.id
                and o.status in ('CONFIRMED', 'WAITING_FOR_DRIVER', 'NO_DRIVER_AVAILABLE', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED')
            left join lateral (
                select pc.commission_percentage
                from platform_commissions pc
                where o.id is not null
                  and pc.starts_at <= o.created_at
                  and (pc.ends_at is null or pc.ends_at > o.created_at)
                order by pc.starts_at desc
                limit 1
            ) applied_commission on true
            group by r.id, r.name
            order by commission_amount desc, revenue desc
            """, nativeQuery = true)
    List<Object[]> restaurantCommissionStats();

    @Query(value = """
            select coalesce(sum(o.total_amount), 0)
            from orders o
            where o.status in ('CONFIRMED', 'WAITING_FOR_DRIVER', 'NO_DRIVER_AVAILABLE', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED')
            """, nativeQuery = true)
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
                   coalesce(sum(o.delivery_fee), 0)
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
            where o.status in ('CONFIRMED', 'WAITING_FOR_DRIVER', 'NO_DRIVER_AVAILABLE', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED')
            group by oi.product_id, oi.product_name, r.name
            order by coalesce(sum(oi.quantity), 0) desc
            limit 10
            """, nativeQuery = true)
    List<Object[]> topProducts();

    @Query(value = "select count(*) from complaints where status in ('OPEN', 'IN_PROGRESS')", nativeQuery = true)
    long openComplaints();

    @Query(value = """
            select coalesce(sum(o.subtotal_amount * coalesce(applied_commission.commission_percentage, 0) / 100), 0)
            from orders o
            left join lateral (
                select pc.commission_percentage
                from platform_commissions pc
                where pc.starts_at <= o.created_at
                  and (pc.ends_at is null or pc.ends_at > o.created_at)
                order by pc.starts_at desc
                limit 1
            ) applied_commission on true
            where o.status in ('CONFIRMED', 'WAITING_FOR_DRIVER', 'NO_DRIVER_AVAILABLE', 'PREPARING', 'READY_FOR_PICKUP', 'ON_THE_WAY', 'DELIVERED')
            """, nativeQuery = true)
    BigDecimal estimatedCommissions();
}
