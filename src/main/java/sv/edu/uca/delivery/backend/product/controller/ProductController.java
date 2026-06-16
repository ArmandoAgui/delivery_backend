package sv.edu.uca.delivery.backend.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.UUID;import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/available")
    public List<ProductResponseDTO> findAvailable() {
        return productService.findAvailable();
    }


    @GetMapping("/category/{categoryId}")
    public List<ProductResponseDTO> findByCategory(
            @PathVariable Long categoryId
    ) {
        return productService.findByCategory(categoryId);
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
