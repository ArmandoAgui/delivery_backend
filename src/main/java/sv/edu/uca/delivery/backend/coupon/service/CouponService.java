package sv.edu.uca.delivery.backend.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.coupon.dto.CouponRequest;
import sv.edu.uca.delivery.backend.coupon.dto.CouponResponse;
import sv.edu.uca.delivery.backend.coupon.entity.Coupon;
import sv.edu.uca.delivery.backend.coupon.repository.CouponRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse create(CouponRequest request) {
        if (couponRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Coupon code already exists");
        }
        Coupon coupon = new Coupon();
        apply(coupon, request);
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> findAll() {
        return couponRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CouponResponse findById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public CouponResponse update(Long id, CouponRequest request) {
        Coupon coupon = find(id);
        apply(coupon, request);
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse setActive(Long id, boolean active) {
        Coupon coupon = find(id);
        coupon.setActive(active);
        return toResponse(couponRepository.save(coupon));
    }

    private Coupon find(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Coupon not found"));
    }

    private void apply(Coupon coupon, CouponRequest request) {
        if (request.expiresAt().isBefore(request.startsAt())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Coupon expiration must be after start date");
        }
        coupon.setCode(request.code().trim().toUpperCase());
        coupon.setDescription(request.description());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinimumOrderAmount(request.minimumOrderAmount());
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setStartsAt(request.startsAt());
        coupon.setExpiresAt(request.expiresAt());
        coupon.setActive(request.active());
    }

    private CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinimumOrderAmount(),
                coupon.getMaxDiscountAmount(),
                coupon.getUsageLimit(),
                coupon.getUsedCount(),
                coupon.getStartsAt(),
                coupon.getExpiresAt(),
                coupon.isActive()
        );
    }
}
