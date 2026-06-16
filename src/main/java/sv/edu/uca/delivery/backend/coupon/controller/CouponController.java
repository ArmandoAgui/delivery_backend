package sv.edu.uca.delivery.backend.coupon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.coupon.dto.CouponRequest;
import sv.edu.uca.delivery.backend.coupon.dto.CouponResponse;
import sv.edu.uca.delivery.backend.coupon.service.CouponService;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@RequestBody @Valid CouponRequest request) {
        return couponService.create(request);
    }

    @GetMapping
    public List<CouponResponse> findAll() {
        return couponService.findAll();
    }

    @GetMapping("/{id}")
    public CouponResponse findById(@PathVariable Long id) {
        return couponService.findById(id);
    }

    @PutMapping("/{id}")
    public CouponResponse update(@PathVariable Long id, @RequestBody @Valid CouponRequest request) {
        return couponService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public CouponResponse activate(@PathVariable Long id) {
        return couponService.setActive(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    public CouponResponse deactivate(@PathVariable Long id) {
        return couponService.setActive(id, false);
    }
}
