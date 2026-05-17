package sv.edu.uca.delivery.backend.restaurant.service;

import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantCreateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantUpdateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.response.RestaurantResponseDTO;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    RestaurantResponseDTO create(RestaurantCreateDTO dto);

    List<RestaurantResponseDTO> findAll();

    RestaurantResponseDTO findById(UUID id);

    RestaurantResponseDTO update(UUID id, RestaurantUpdateDTO dto);

    void softDelete(UUID id);

    List<RestaurantResponseDTO> findOpenRestaurants();
}
