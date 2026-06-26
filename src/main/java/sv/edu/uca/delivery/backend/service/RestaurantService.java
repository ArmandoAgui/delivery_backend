package sv.edu.uca.delivery.backend.service;

import sv.edu.uca.delivery.backend.dto.RestaurantCreateDTO;
import sv.edu.uca.delivery.backend.dto.RestaurantScheduleDTO;
import sv.edu.uca.delivery.backend.dto.RestaurantScheduleRequestDTO;
import sv.edu.uca.delivery.backend.dto.RestaurantUpdateDTO;
import sv.edu.uca.delivery.backend.dto.RestaurantResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    RestaurantResponseDTO create(RestaurantCreateDTO dto);

    List<RestaurantResponseDTO> findAll();

    List<RestaurantResponseDTO> findAllForAdmin();

    RestaurantResponseDTO findMine();

    RestaurantResponseDTO findById(UUID id);

    List<RestaurantResponseDTO> search(String query);

    List<RestaurantResponseDTO> searchForAdmin(String query);

    RestaurantResponseDTO update(UUID id, RestaurantUpdateDTO dto);

    RestaurantResponseDTO uploadImage(UUID id, MultipartFile file);

    void deleteImage(UUID id);

    void softDelete(UUID id);

    RestaurantResponseDTO activate(UUID id);

    List<RestaurantResponseDTO> findOpenRestaurants();

    List<RestaurantResponseDTO> findNearby(double latitude, double longitude, double radiusKm);

    List<RestaurantScheduleDTO> findSchedules(UUID restaurantId);

    List<RestaurantScheduleDTO> updateSchedules(UUID restaurantId, List<RestaurantScheduleRequestDTO> schedules);
}
