package sv.edu.uca.delivery.backend.mapper;

import sv.edu.uca.delivery.backend.dto.CategoryResponseDTO;
import sv.edu.uca.delivery.backend.entity.Category;

public class CategoryMapper {

    public static CategoryResponseDTO toDTO(Category category) {

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .restaurantId(category.getRestaurant().getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}