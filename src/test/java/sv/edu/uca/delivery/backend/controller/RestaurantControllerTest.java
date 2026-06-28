package sv.edu.uca.delivery.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sv.edu.uca.delivery.backend.exception.GlobalExceptionHandler;
import sv.edu.uca.delivery.backend.dto.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.exception.RestaurantNotFoundException;
import sv.edu.uca.delivery.backend.service.RestaurantService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RestaurantController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    @Test
    void createReturnsCreatedRestaurant() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(restaurantService.create(any())).thenReturn(response(restaurantId, ownerId, "Comedor Central", true, true));

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerId":"%s",
                                  "name":"Comedor Central",
                                  "description":"Comida salvadorena",
                                  "phone":"2222-3333",
                                  "email":"comedor@example.com",
                                  "streetAddress":"Boulevard Los Proceres",
                                  "department":"San Salvador",
                                  "latitude":13.6929,
                                  "longitude":-89.2182,
                                  "open":true
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(restaurantId.toString()))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andExpect(jsonPath("$.name").value("Comedor Central"))
                .andExpect(jsonPath("$.streetAddress").value("Boulevard Los Proceres"))
                .andExpect(jsonPath("$.department").value("San Salvador"))
                .andExpect(jsonPath("$.latitude").value(13.6929))
                .andExpect(jsonPath("$.longitude").value(-89.2182))
                .andExpect(jsonPath("$.open").value(true))
                .andExpect(jsonPath("$.active").value(true));

        verify(restaurantService).create(any());
    }

    @Test
    void createReturnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request payload"));

        verify(restaurantService, never()).create(any());
    }

    @Test
    void findAllReturnsRestaurants() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(restaurantService.findAll()).thenReturn(List.of(response(restaurantId, ownerId, "La Terraza", false, true)));

        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(restaurantId.toString()))
                .andExpect(jsonPath("$[0].name").value("La Terraza"));

        verify(restaurantService).findAll();
    }

    @Test
    void findByIdReturnsNotFoundWhenRestaurantDoesNotExist() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantService.findById(restaurantId)).thenThrow(new RestaurantNotFoundException());

        mockMvc.perform(get("/restaurants/{id}", restaurantId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Restaurant not found"));
    }

    @Test
    void updateReturnsUpdatedRestaurant() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(restaurantService.update(eq(restaurantId), any()))
                .thenReturn(response(restaurantId, ownerId, "Nuevo Nombre", true, true));

        mockMvc.perform(put("/restaurants/{id}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Nuevo Nombre",
                                  "description":"Comida salvadorena",
                                  "phone":"2222-3333",
                                  "email":"comedor@example.com",
                                  "streetAddress":"Boulevard Los Proceres",
                                  "department":"San Salvador",
                                  "latitude":13.6929,
                                  "longitude":-89.2182,
                                  "open":true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.streetAddress").value("Boulevard Los Proceres"))
                .andExpect(jsonPath("$.open").value(true))
                .andExpect(jsonPath("$.active").value(true));

        verify(restaurantService).update(eq(restaurantId), any());
    }

    @Test
    void softDeleteReturnsNoContent() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        mockMvc.perform(patch("/restaurants/{id}/deactivate", restaurantId))
                .andExpect(status().isNoContent());

        verify(restaurantService).softDelete(restaurantId);
    }

    @Test
    void findOpenRestaurantsReturnsOpenRestaurants() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(restaurantService.findOpenRestaurants())
                .thenReturn(List.of(response(restaurantId, ownerId, "Abierto", true, true)));

        mockMvc.perform(get("/restaurants/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].open").value(true))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(restaurantService).findOpenRestaurants();
    }

    private RestaurantResponseDTO response(
            UUID restaurantId,
            UUID ownerId,
            String name,
            boolean open,
            boolean active
    ) {
        return RestaurantResponseDTO.builder()
                .id(restaurantId)
                .ownerId(ownerId)
                .name(name)
                .description("Comida salvadorena")
                .phone("2222-3333")
                .email("comedor@example.com")
                .streetAddress("Boulevard Los Proceres")
                .department("San Salvador")
                .latitude(13.6929)
                .longitude(-89.2182)
                .open(open)
                .active(active)
                .createdAt(LocalDateTime.of(2026, 5, 17, 10, 0))
                .build();
    }
}
