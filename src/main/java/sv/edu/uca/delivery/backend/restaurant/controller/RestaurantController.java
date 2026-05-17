package sv.edu.uca.delivery.backend.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantCreateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantUpdateDTO;
import sv.edu.uca.delivery.backend.restaurant.service.RestaurantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public RestaurantResponseDTO create(
            @RequestBody @Valid RestaurantCreateDTO dto
    ) {
        return restaurantService.create(dto);
    }

    @GetMapping
    public List<RestaurantResponseDTO> findAll() {
        return restaurantService.findAll();
    }

    @GetMapping("/{id}")
    public RestaurantResponseDTO findById(
            @PathVariable UUID id
    ) {
        return restaurantService.findById(id);
    }

    @PutMapping("/{id}")
    public RestaurantResponseDTO update(
            @PathVariable UUID id,
            @RequestBody @Valid RestaurantUpdateDTO dto
    ) {
        return restaurantService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id
    ) {
        restaurantService.delete(id);
    }

    @GetMapping("/open")
    public List<RestaurantResponseDTO> findOpenRestaurants() {
        return restaurantService.findOpenRestaurants();
    }
}