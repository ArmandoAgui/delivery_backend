package sv.edu.uca.delivery.backend.service;

import sv.edu.uca.delivery.backend.dto.CategoryCreateDTO;
import sv.edu.uca.delivery.backend.dto.CategoryUpdateDTO;
import sv.edu.uca.delivery.backend.dto.CategoryResponseDTO;

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