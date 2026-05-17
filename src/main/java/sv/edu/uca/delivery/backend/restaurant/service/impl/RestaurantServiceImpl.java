package sv.edu.uca.delivery.backend.restaurant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantCreateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantUpdateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.response.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.restaurant.exception.RestaurantNotFoundException;
import sv.edu.uca.delivery.backend.restaurant.exception.RestaurantOwnerNotFoundException;
import sv.edu.uca.delivery.backend.restaurant.mapper.RestaurantMapper;
import sv.edu.uca.delivery.backend.restaurant.repository.RestaurantRepository;
import sv.edu.uca.delivery.backend.restaurant.service.RestaurantService;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    public RestaurantResponseDTO create(RestaurantCreateDTO dto) {

        User owner = userRepository.findActiveUserByIdAndRole(dto.getOwnerId(), RoleName.RESTAURANT)
                .orElseThrow(RestaurantOwnerNotFoundException::new);

        Restaurant restaurant = new Restaurant();

        restaurant.setOwner(owner);
        applyCreateFields(restaurant, dto);
        restaurant.setOpen(dto.isOpen());

        restaurantRepository.save(restaurant);

        return RestaurantMapper.toDTO(restaurant);
    }

    @Override
    public List<RestaurantResponseDTO> findAll() {

        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(RestaurantMapper::toDTO)
                .toList();
    }

    @Override
    public RestaurantResponseDTO findById(UUID id) {

        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(RestaurantNotFoundException::new);

        return RestaurantMapper.toDTO(restaurant);
    }

    @Override
    public RestaurantResponseDTO update(UUID id, RestaurantUpdateDTO dto) {

        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(RestaurantNotFoundException::new);

        applyUpdateFields(restaurant, dto);
        restaurant.setOpen(dto.isOpen());

        restaurantRepository.save(restaurant);

        return RestaurantMapper.toDTO(restaurant);
    }

    @Override
    public void softDelete(UUID id) {

        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(RestaurantNotFoundException::new);

        restaurant.setActive(false);

        restaurantRepository.save(restaurant);
    }

    @Override
    public List<RestaurantResponseDTO> findOpenRestaurants() {

        return restaurantRepository.findByOpenTrueAndActiveTrue()
                .stream()
                .map(RestaurantMapper::toDTO)
                .toList();
    }

    private void applyCreateFields(Restaurant restaurant, RestaurantCreateDTO dto) {
        restaurant.setName(dto.getName());
        restaurant.setDescription(dto.getDescription());
        restaurant.setPhone(dto.getPhone());
        restaurant.setEmail(dto.getEmail());
        restaurant.setStreetAddress(dto.getStreetAddress());
        restaurant.setCity(dto.getCity());
        restaurant.setState(dto.getState());
        restaurant.setCountry(dto.getCountry());
        restaurant.setLocation(RestaurantMapper.toLocation(dto.getLatitude(), dto.getLongitude()));
    }

    private void applyUpdateFields(Restaurant restaurant, RestaurantUpdateDTO dto) {
        restaurant.setName(dto.getName());
        restaurant.setDescription(dto.getDescription());
        restaurant.setPhone(dto.getPhone());
        restaurant.setEmail(dto.getEmail());
        restaurant.setStreetAddress(dto.getStreetAddress());
        restaurant.setCity(dto.getCity());
        restaurant.setState(dto.getState());
        restaurant.setCountry(dto.getCountry());
        restaurant.setLocation(RestaurantMapper.toLocation(dto.getLatitude(), dto.getLongitude()));
    }
}
