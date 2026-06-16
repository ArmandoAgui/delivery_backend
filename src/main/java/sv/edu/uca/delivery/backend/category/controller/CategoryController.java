package sv.edu.uca.delivery.backend.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.category.dto.CategoryCreateDTO;
import sv.edu.uca.delivery.backend.category.dto.CategoryUpdateDTO;
import sv.edu.uca.delivery.backend.category.dto.response.CategoryResponseDTO;
import sv.edu.uca.delivery.backend.category.service.CategoryService;
import sv.edu.uca.delivery.backend.common.pagination.PageResponse;
import sv.edu.uca.delivery.backend.common.pagination.PaginationUtils;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/categories", "/api/categories"})
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO create(
            @RequestBody @Valid CategoryCreateDTO dto
    ) {
        return categoryService.create(dto);
    }

    @GetMapping
    public List<CategoryResponseDTO> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/page")
    public PageResponse<CategoryResponseDTO> findAllPaged(Pageable pageable) {
        return PaginationUtils.toPage(categoryService.findAll(), pageable);
    }

    @GetMapping("/{id}")
    public CategoryResponseDTO findById(
            @PathVariable Long id
    ) {
        return categoryService.findById(id);
    }

    @PutMapping("/{id}")
    public CategoryResponseDTO update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryUpdateDTO dto
    ) {
        return categoryService.update(id, dto);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(
            @PathVariable Long id
    ) {
        categoryService.softDelete(id);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<CategoryResponseDTO> findByRestaurant(
            @PathVariable UUID restaurantId
    ) {
        return categoryService.findByRestaurant(restaurantId);
    }

    @GetMapping("/restaurant/{restaurantId}/page")
    public PageResponse<CategoryResponseDTO> findByRestaurantPaged(
            @PathVariable UUID restaurantId,
            Pageable pageable
    ) {
        return PaginationUtils.toPage(categoryService.findByRestaurant(restaurantId), pageable);
    }
}
