package sv.edu.uca.delivery.backend.category.mapper;

import sv.edu.uca.delivery.backend.category.dto.response.CategoryResponseDTO;
import sv.edu.uca.delivery.backend.category.entity.Category;

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