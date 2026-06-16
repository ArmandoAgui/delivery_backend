package sv.edu.uca.delivery.backend.product.controller;

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
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO create(
            @RequestBody @Valid ProductCreateDTO dto
    ) {
        return productService.create(dto);
    }

    @GetMapping
    public List<ProductResponseDTO> findAll() {
        return productService.findAll();
    }

    @GetMapping("/page")
    public PageResponse<ProductResponseDTO> findAllPaged(Pageable pageable) {
        return PaginationUtils.toPage(productService.findAll(), pageable);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO findById(
            @PathVariable UUID id
    ) {
        return productService.findById(id);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO update(
            @PathVariable UUID id,
            @RequestBody @Valid ProductUpdateDTO dto
    ) {
        return productService.update(id, dto);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(
            @PathVariable UUID id
    ) {
        productService.softDelete(id);
    }

    @GetMapping("/restaurant/{restaurantId}")
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
    public List<ProductResponseDTO> findAvailable() {
        return productService.findAvailable();
    }

    @GetMapping("/available/page")
    public PageResponse<ProductResponseDTO> findAvailablePaged(Pageable pageable) {
        return PaginationUtils.toPage(productService.findAvailable(), pageable);
    }


    @GetMapping("/category/{categoryId}")
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
