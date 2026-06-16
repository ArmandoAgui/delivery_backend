package sv.edu.uca.delivery.backend.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import java.util.UUID;import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.common.pagination.PageResponse;
import sv.edu.uca.delivery.backend.common.pagination.PaginationUtils;
import sv.edu.uca.delivery.backend.product.dto.ProductCreateDTO;
import sv.edu.uca.delivery.backend.product.dto.ProductUpdateDTO;
import sv.edu.uca.delivery.backend.product.dto.response.ProductResponseDTO;
import sv.edu.uca.delivery.backend.product.service.ProductService;
import sv.edu.uca.delivery.backend.product.dto.ProductAvailabilityDTO;


import java.util.List;

@RestController
@RequestMapping({"/products", "/api/products"})
@RequiredArgsConstructor
@Tag(name = "Products", description = "Productos/menu, precios, disponibilidad y categorias.")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear producto")
    public ProductResponseDTO create(
            @RequestBody @Valid ProductCreateDTO dto
    ) {
        return productService.create(dto);
    }

    @GetMapping
    @Operation(summary = "Listar productos")
    public List<ProductResponseDTO> findAll() {
        return productService.findAll();
    }

    @GetMapping("/page")
    @Operation(summary = "Listar productos paginados")
    public PageResponse<ProductResponseDTO> findAllPaged(Pageable pageable) {
        return PaginationUtils.toPage(productService.findAll(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar producto por ID")
    public ProductResponseDTO findById(
            @PathVariable UUID id
    ) {
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ProductResponseDTO update(
            @PathVariable UUID id,
            @RequestBody @Valid ProductUpdateDTO dto
    ) {
        return productService.update(id, dto);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar producto")
    public void softDelete(
            @PathVariable UUID id
    ) {
        productService.softDelete(id);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Listar productos por restaurante")
    public List<ProductResponseDTO> findByRestaurant(
            @PathVariable UUID restaurantId
    ) {
        return productService.findByRestaurant(restaurantId);
    }

    @GetMapping("/restaurant/{restaurantId}/page")
    public PageResponse<ProductResponseDTO> findByRestaurantPaged(
            @PathVariable UUID restaurantId,
            Pageable pageable
    ) {
        return PaginationUtils.toPage(productService.findByRestaurant(restaurantId), pageable);
    }

    @GetMapping("/available")
    @Operation(summary = "Listar productos disponibles")
    public List<ProductResponseDTO> findAvailable() {
        return productService.findAvailable();
    }

    @GetMapping("/available/page")
    public PageResponse<ProductResponseDTO> findAvailablePaged(Pageable pageable) {
        return PaginationUtils.toPage(productService.findAvailable(), pageable);
    }


    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar productos por categoria")
    public List<ProductResponseDTO> findByCategory(
            @PathVariable Long categoryId
    ) {
        return productService.findByCategory(categoryId);
    }

    @GetMapping("/category/{categoryId}/page")
    public PageResponse<ProductResponseDTO> findByCategoryPaged(
            @PathVariable Long categoryId,
            Pageable pageable
    ) {
        return PaginationUtils.toPage(productService.findByCategory(categoryId), pageable);
    }


    @PatchMapping("/{id}/availability")
    @Operation(summary = "Actualizar disponibilidad de producto")
    public ProductResponseDTO updateAvailability(
            @PathVariable UUID id,
            @RequestBody ProductAvailabilityDTO dto
    ) {
        return productService.updateAvailability(
                id,
                dto.isAvailable()
        );
    }

}
