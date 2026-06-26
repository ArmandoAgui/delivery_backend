package sv.edu.uca.delivery.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.dto.CategoryCreateDTO;
import sv.edu.uca.delivery.backend.dto.CategoryUpdateDTO;
import sv.edu.uca.delivery.backend.dto.CategoryResponseDTO;
import sv.edu.uca.delivery.backend.service.CategoryService;
import sv.edu.uca.delivery.backend.util.PageResponse;
import sv.edu.uca.delivery.backend.util.PaginationUtils;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/categories", "/api/categories"})
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Categorias de menu por restaurante.")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoria")
    public CategoryResponseDTO create(
            @RequestBody @Valid CategoryCreateDTO dto
    ) {
        return categoryService.create(dto);
    }

    @GetMapping
    @Operation(summary = "Listar categorias")
    public List<CategoryResponseDTO> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/page")
    @Operation(summary = "Listar categorias paginadas")
    public PageResponse<CategoryResponseDTO> findAllPaged(Pageable pageable) {
        return PaginationUtils.toPage(categoryService.findAll(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoria por ID")
    public CategoryResponseDTO findById(
            @PathVariable Long id
    ) {
        return categoryService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoria")
    public CategoryResponseDTO update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryUpdateDTO dto
    ) {
        return categoryService.update(id, dto);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar categoria")
    public void softDelete(
            @PathVariable Long id
    ) {
        categoryService.softDelete(id);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Listar categorias por restaurante")
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
