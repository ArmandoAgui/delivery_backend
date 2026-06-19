package sv.edu.uca.delivery.backend.restaurant.service;

import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantCreateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantScheduleDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantScheduleRequestDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantUpdateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.response.RestaurantResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    RestaurantResponseDTO create(RestaurantCreateDTO dto);

    List<RestaurantResponseDTO> findAll();

    RestaurantResponseDTO findMine();

    RestaurantResponseDTO findById(UUID id);

    List<RestaurantResponseDTO> search(String query);

    RestaurantResponseDTO update(UUID id, RestaurantUpdateDTO dto);

    RestaurantResponseDTO uploadImage(UUID id, MultipartFile file);

    void deleteImage(UUID id);

    void softDelete(UUID id);

    List<RestaurantResponseDTO> findOpenRestaurants();

    List<RestaurantResponseDTO> findNearby(double latitude, double longitude, double radiusKm);

    List<RestaurantScheduleDTO> findSchedules(UUID restaurantId);

    List<RestaurantScheduleDTO> updateSchedules(UUID restaurantId, List<RestaurantScheduleRequestDTO> schedules);
}
