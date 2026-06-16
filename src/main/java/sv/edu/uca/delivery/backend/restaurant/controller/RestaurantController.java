package sv.edu.uca.delivery.backend.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantCreateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantScheduleDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantScheduleRequestDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantUpdateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.response.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.restaurant.service.RestaurantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/restaurants", "/api/restaurants"})
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(
            @PathVariable UUID id
    ) {
        restaurantService.softDelete(id);
    }

    @GetMapping("/open")
    public List<RestaurantResponseDTO> findOpenRestaurants() {
        return restaurantService.findOpenRestaurants();
    }

    @GetMapping("/{id}/schedules")
    public List<RestaurantScheduleDTO> findSchedules(
            @PathVariable UUID id
    ) {
        return restaurantService.findSchedules(id);
    }

    @PutMapping("/{id}/schedules")
    public List<RestaurantScheduleDTO> updateSchedules(
            @PathVariable UUID id,
            @RequestBody @Valid List<@Valid RestaurantScheduleRequestDTO> schedules
    ) {
        return restaurantService.updateSchedules(id, schedules);
    }
}
