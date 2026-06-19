package sv.edu.uca.delivery.backend.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.coupon.entity.CouponRedemption;

import java.util.UUID;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, UUID> {

    boolean existsByCouponIdAndOrderId(Long couponId, UUID orderId);
}
