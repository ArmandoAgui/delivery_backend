package sv.edu.uca.delivery.backend.restaurant.mapper;

import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;

public class RestaurantMapper {

    public static RestaurantResponseDTO toDTO(Restaurant restaurant) {

        return RestaurantResponseDTO.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwner().getId())
                .name(restaurant.getName())
                .open(restaurant.isOpen())
                .active(restaurant.isActive())
                .createdAt(restaurant.getCreatedAt())
                .build();
    }
}