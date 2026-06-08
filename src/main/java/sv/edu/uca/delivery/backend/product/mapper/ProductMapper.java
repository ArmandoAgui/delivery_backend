package sv.edu.uca.delivery.backend.product.mapper;

import sv.edu.uca.delivery.backend.product.dto.response.ProductResponseDTO;
import sv.edu.uca.delivery.backend.product.entity.Product;

public class ProductMapper {

    public static ProductResponseDTO toDTO(Product product) {

        return ProductResponseDTO.builder()
                .id(product.getId())
                .restaurantId(product.getRestaurant().getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .available(product.isAvailable())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}