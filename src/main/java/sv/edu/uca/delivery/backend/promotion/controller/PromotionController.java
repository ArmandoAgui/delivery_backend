package sv.edu.uca.delivery.backend.promotion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.common.pagination.PageResponse;
import sv.edu.uca.delivery.backend.common.pagination.PaginationUtils;
import sv.edu.uca.delivery.backend.promotion.dto.PromotionCreateDTO;
import sv.edu.uca.delivery.backend.promotion.dto.PromotionStatusDTO;
import sv.edu.uca.delivery.backend.promotion.dto.PromotionUpdateDTO;
import sv.edu.uca.delivery.backend.promotion.dto.response.PromotionResponseDTO;
import sv.edu.uca.delivery.backend.promotion.service.PromotionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/promotions", "/api/promotions"})
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Promociones del catalogo por restaurante.")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear promocion")
    public PromotionResponseDTO create(
            @RequestBody @Valid PromotionCreateDTO dto
    ) {
        return promotionService.create(dto);
    }

    @GetMapping
    @Operation(summary = "Listar promociones")
    public List<PromotionResponseDTO> findAll() {
        return promotionService.findAll();
    }

    @GetMapping("/page")
    public PageResponse<PromotionResponseDTO> findAllPaged(Pageable pageable) {
        return PaginationUtils.toPage(promotionService.findAll(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar promocion por ID")
    public PromotionResponseDTO findById(
            @PathVariable Long id
    ) {
        return promotionService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar promocion")
    public PromotionResponseDTO update(
            @PathVariable Long id,
            @RequestBody @Valid PromotionUpdateDTO dto
    ) {
        return promotionService.update(id, dto);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar promocion")
    public void softDelete(
            @PathVariable Long id
    ) {
        promotionService.softDelete(id);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Listar promociones por restaurante")
    public List<PromotionResponseDTO> findByRestaurant(
            @PathVariable UUID restaurantId
    ) {
        return promotionService.findByRestaurant(restaurantId);
    }

    @GetMapping("/restaurant/{restaurantId}/page")
    public PageResponse<PromotionResponseDTO> findByRestaurantPaged(
            @PathVariable UUID restaurantId,
            Pageable pageable
    ) {
        return PaginationUtils.toPage(promotionService.findByRestaurant(restaurantId), pageable);
    }

    @GetMapping("/active")
    @Operation(summary = "Listar promociones activas")
    public List<PromotionResponseDTO> findActivePromotions() {
        return promotionService.findActivePromotions();
    }

    @GetMapping("/active/page")
    public PageResponse<PromotionResponseDTO> findActivePromotionsPaged(Pageable pageable) {
        return PaginationUtils.toPage(promotionService.findActivePromotions(), pageable);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de promocion")
    public PromotionResponseDTO updateStatus(
            @PathVariable Long id,
            @RequestBody PromotionStatusDTO dto
    ) {
        return promotionService.updateStatus(
                id,
                dto.isActive()
        );
    }
}
