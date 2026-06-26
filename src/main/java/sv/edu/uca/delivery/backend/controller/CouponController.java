package sv.edu.uca.delivery.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import sv.edu.uca.delivery.backend.dto.CouponRequest;
import sv.edu.uca.delivery.backend.dto.CouponResponse;
import sv.edu.uca.delivery.backend.service.CouponService;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Administracion de cupones, vigencia, activacion y desactivacion.")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cupon", description = "Crea un cupon con reglas de descuento, vigencia y monto minimo.")
    public CouponResponse create(@RequestBody @Valid CouponRequest request) {
        return couponService.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar cupones", description = "Obtiene todos los cupones registrados.")
    public List<CouponResponse> findAll() {
        return couponService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar cupon", description = "Obtiene un cupon por identificador.")
    public CouponResponse findById(@PathVariable Long id) {
        return couponService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cupon", description = "Actualiza las reglas y datos principales de un cupon.")
    public CouponResponse update(@PathVariable Long id, @RequestBody @Valid CouponRequest request) {
        return couponService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar cupon", description = "Marca un cupon como disponible para su uso.")
    public CouponResponse activate(@PathVariable Long id) {
        return couponService.setActive(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar cupon", description = "Deshabilita un cupon sin eliminar su historial.")
    public CouponResponse deactivate(@PathVariable Long id) {
        return couponService.setActive(id, false);
    }
}
