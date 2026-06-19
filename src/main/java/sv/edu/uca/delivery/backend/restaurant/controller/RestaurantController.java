package sv.edu.uca.delivery.backend.restaurant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sv.edu.uca.delivery.backend.common.pagination.PageResponse;
import sv.edu.uca.delivery.backend.common.pagination.PaginationUtils;
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
@Tag(name = "Restaurants", description = "Restaurantes, horarios, estado y busquedas publicas.")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear restaurante")
    public RestaurantResponseDTO create(
            @RequestBody @Valid RestaurantCreateDTO dto
    ) {
        return restaurantService.create(dto);
    }

    @GetMapping
    @Operation(summary = "Listar restaurantes activos")
    public List<RestaurantResponseDTO> findAll() {
        return restaurantService.findAll();
    }

    @GetMapping("/page")
    @Operation(summary = "Listar restaurantes paginados")
    public PageResponse<RestaurantResponseDTO> findAllPaged(Pageable pageable) {
        return PaginationUtils.toPage(restaurantService.findAll(), pageable);
    }

    @GetMapping("/my")
    @Operation(summary = "Consultar restaurante propio", description = "Devuelve el restaurante asociado al usuario RESTAURANT autenticado.")
    public RestaurantResponseDTO findMine() {
        return restaurantService.findMine();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar restaurante por ID")
    public RestaurantResponseDTO findById(
            @PathVariable UUID id
    ) {
        return restaurantService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar restaurante")
    public RestaurantResponseDTO update(
            @PathVariable UUID id,
            @RequestBody @Valid RestaurantUpdateDTO dto
    ) {
        return restaurantService.update(id, dto);
    }

    @PostMapping("/{id}/image")
    @Operation(summary = "Subir o reemplazar imagen del restaurante")
    public RestaurantResponseDTO uploadImage(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file
    ) {
        return restaurantService.uploadImage(id, file);
    }

    @DeleteMapping("/{id}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar imagen del restaurante")
    public void deleteImage(@PathVariable UUID id) {
        restaurantService.deleteImage(id);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar restaurante")
    public void softDelete(
            @PathVariable UUID id
    ) {
        restaurantService.softDelete(id);
    }

    @GetMapping("/open")
    @Operation(summary = "Listar restaurantes abiertos")
    public List<RestaurantResponseDTO> findOpenRestaurants() {
        return restaurantService.findOpenRestaurants();
    }

    @GetMapping("/open/page")
    public PageResponse<RestaurantResponseDTO> findOpenRestaurantsPaged(Pageable pageable) {
        return PaginationUtils.toPage(restaurantService.findOpenRestaurants(), pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar restaurantes por texto", description = "Busca por nombre, ciudad o descripcion.")
    public List<RestaurantResponseDTO> search(@RequestParam(name = "q", required = false) String query) {
        return restaurantService.search(query);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Buscar restaurantes cercanos", description = "Usa PostGIS para ordenar restaurantes activos dentro del radio indicado.")
    public List<RestaurantResponseDTO> findNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm
    ) {
        return restaurantService.findNearby(lat, lng, radiusKm);
    }

    @GetMapping("/{id}/schedules")
    @Operation(summary = "Consultar horarios del restaurante")
    public List<RestaurantScheduleDTO> findSchedules(
            @PathVariable UUID id
    ) {
        return restaurantService.findSchedules(id);
    }

    @PutMapping("/{id}/schedules")
    @Operation(summary = "Actualizar horarios del restaurante")
    public List<RestaurantScheduleDTO> updateSchedules(
            @PathVariable UUID id,
            @RequestBody @Valid List<@Valid RestaurantScheduleRequestDTO> schedules
    ) {
        return restaurantService.updateSchedules(id, schedules);
    }
}
