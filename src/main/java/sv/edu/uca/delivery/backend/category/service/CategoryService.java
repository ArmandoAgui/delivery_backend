package sv.edu.uca.delivery.backend.category.service;

import sv.edu.uca.delivery.backend.category.dto.CategoryCreateDTO;
import sv.edu.uca.delivery.backend.category.dto.CategoryUpdateDTO;
import sv.edu.uca.delivery.backend.category.dto.response.CategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponseDTO create(CategoryCreateDTO dto);

    List<CategoryResponseDTO> findAll();

    CategoryResponseDTO findById(Long id);

    CategoryResponseDTO update(Long id, CategoryUpdateDTO dto);

    void softDelete(Long id);

    List<CategoryResponseDTO> findByRestaurant(UUID restaurantId);
}