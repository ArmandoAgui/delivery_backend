package sv.edu.uca.delivery.backend.restaurant.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantCreateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantScheduleRequestDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantUpdateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.response.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.restaurant.entity.RestaurantSchedule;
import sv.edu.uca.delivery.backend.restaurant.exception.RestaurantNotFoundException;
import sv.edu.uca.delivery.backend.restaurant.exception.RestaurantOwnerAlreadyHasRestaurantException;
import sv.edu.uca.delivery.backend.restaurant.exception.RestaurantOwnerNotFoundException;
import sv.edu.uca.delivery.backend.restaurant.exception.RestaurantScheduleInvalidException;
import sv.edu.uca.delivery.backend.restaurant.mapper.RestaurantMapper;
import sv.edu.uca.delivery.backend.restaurant.repository.RestaurantRepository;
import sv.edu.uca.delivery.backend.restaurant.repository.RestaurantScheduleRepository;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantScheduleRepository restaurantScheduleRepository;

    private RestaurantServiceImpl restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantServiceImpl(
                restaurantRepository,
                userRepository,
                restaurantScheduleRepository
        );
    }

    @Test
    void createPersistsRestaurantForOwner() {
        User owner = user();
        RestaurantCreateDTO request = new RestaurantCreateDTO();
        request.setOwnerId(owner.getId());
        request.setName("Pupuseria Central");
        applyLocationFields(request);
        request.setOpen(true);

        when(userRepository.findActiveUserByIdAndRole(owner.getId(), RoleName.RESTAURANT))
                .thenReturn(Optional.of(owner));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0);
            restaurant.setId(UUID.randomUUID());
            restaurant.setCreatedAt(LocalDateTime.of(2026, 5, 17, 10, 0));
            return restaurant;
        });
        when(restaurantScheduleRepository.findByRestaurantIdAndDayOfWeek(any(), anyShort()))
                .thenReturn(Optional.of(openSchedule()));

        RestaurantResponseDTO response = restaurantService.create(request);

        ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepository).save(restaurantCaptor.capture());
        assertThat(restaurantCaptor.getValue().getOwner()).isSameAs(owner);
        assertThat(restaurantCaptor.getValue().getName()).isEqualTo("Pupuseria Central");
        assertThat(restaurantCaptor.getValue().getStreetAddress()).isEqualTo("Boulevard Los Proceres");
        assertThat(restaurantCaptor.getValue().getCity()).isEqualTo("San Salvador");
        assertThat(restaurantCaptor.getValue().getCountry()).isEqualTo("El Salvador");
        assertThat(restaurantCaptor.getValue().getLocation().getY()).isEqualTo(13.6929);
        assertThat(restaurantCaptor.getValue().getLocation().getX()).isEqualTo(-89.2182);
        assertThat(restaurantCaptor.getValue().isOpen()).isFalse();
        assertThat(response.getOwnerId()).isEqualTo(owner.getId());
        assertThat(response.getName()).isEqualTo("Pupuseria Central");
        assertThat(response.getLatitude()).isEqualTo(13.6929);
        assertThat(response.getLongitude()).isEqualTo(-89.2182);
        assertThat(response.isOpen()).isTrue();
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void createRejectsOwnerThatAlreadyHasRestaurant() {
        User owner = user();
        RestaurantCreateDTO request = new RestaurantCreateDTO();
        request.setOwnerId(owner.getId());
        request.setName("Pupuseria Central");
        applyLocationFields(request);

        when(userRepository.findActiveUserByIdAndRole(owner.getId(), RoleName.RESTAURANT))
                .thenReturn(Optional.of(owner));
        when(restaurantRepository.existsByOwnerId(owner.getId())).thenReturn(true);

        assertThatThrownBy(() -> restaurantService.create(request))
                .isInstanceOf(RestaurantOwnerAlreadyHasRestaurantException.class)
                .hasMessage("Restaurant owner already has a restaurant");
    }

    @Test
    void createRejectsOwnerThatIsNotActiveRestaurantUser() {
        UUID ownerId = UUID.randomUUID();
        RestaurantCreateDTO request = new RestaurantCreateDTO();
        request.setOwnerId(ownerId);
        request.setName("Pupuseria Central");
        applyLocationFields(request);

        when(userRepository.findActiveUserByIdAndRole(ownerId, RoleName.RESTAURANT))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.create(request))
                .isInstanceOf(RestaurantOwnerNotFoundException.class)
                .hasMessage("Restaurant owner not found");
    }

    @Test
    void findAllReturnsOnlyActiveRestaurantsFromRepository() {
        Restaurant restaurant = restaurant("Tacos UCA", true, true);
        when(restaurantRepository.findByActiveTrue()).thenReturn(List.of(restaurant));
        when(restaurantScheduleRepository.findByRestaurantIdAndDayOfWeek(any(), anyShort()))
                .thenReturn(Optional.of(openSchedule()));

        List<RestaurantResponseDTO> response = restaurantService.findAll();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getId()).isEqualTo(restaurant.getId());
        assertThat(response.getFirst().getName()).isEqualTo("Tacos UCA");
    }

    @Test
    void findByIdThrowsWhenRestaurantDoesNotExist() {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantRepository.findByIdAndActiveTrue(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.findById(restaurantId))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessage("Restaurant not found");
    }

    @Test
    void updateChangesRestaurantFields() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = restaurant("Original", false, true);
        RestaurantUpdateDTO request = new RestaurantUpdateDTO();
        request.setName("Nuevo Nombre");
        applyLocationFields(request);
        request.setOpen(true);

        when(restaurantRepository.findByIdAndActiveTrue(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restaurantScheduleRepository.findByRestaurantIdAndDayOfWeek(any(), anyShort()))
                .thenReturn(Optional.of(openSchedule()));

        RestaurantResponseDTO response = restaurantService.update(restaurantId, request);

        assertThat(response.getName()).isEqualTo("Nuevo Nombre");
        assertThat(response.getStreetAddress()).isEqualTo("Boulevard Los Proceres");
        assertThat(response.getLatitude()).isEqualTo(13.6929);
        assertThat(response.isOpen()).isTrue();
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void softDeleteMarksRestaurantInactive() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = restaurant("Sushi Demo", true, true);
        when(restaurantRepository.findByIdAndActiveTrue(restaurantId)).thenReturn(Optional.of(restaurant));

        restaurantService.softDelete(restaurantId);

        assertThat(restaurant.isActive()).isFalse();
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void findOpenRestaurantsReturnsOpenAndActiveRestaurantsFromRepository() {
        Restaurant restaurant = restaurant("Cafe Abierto", true, true);
        when(restaurantRepository.findByActiveTrue()).thenReturn(List.of(restaurant));
        when(restaurantScheduleRepository.findByRestaurantIdAndDayOfWeek(any(), anyShort()))
                .thenReturn(Optional.of(openSchedule()));

        List<RestaurantResponseDTO> response = restaurantService.findOpenRestaurants();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().isOpen()).isTrue();
        assertThat(response.getFirst().isActive()).isTrue();
    }

    @Test
    void updateSchedulesRejectsOpenDayWithoutHours() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = restaurant("Cafe Horarios", true, true);
        RestaurantScheduleRequestDTO request = new RestaurantScheduleRequestDTO();
        request.setDayOfWeek(1);
        request.setClosed(false);

        when(restaurantRepository.findByIdAndActiveTrue(restaurantId)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> restaurantService.updateSchedules(restaurantId, List.of(request)))
                .isInstanceOf(RestaurantScheduleInvalidException.class)
                .hasMessage("Open schedule days require opensAt and closesAt");
    }

    @Test
    void updateSchedulesSavesWeeklySchedule() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = restaurant("Cafe Horarios", true, true);
        RestaurantScheduleRequestDTO request = new RestaurantScheduleRequestDTO();
        int currentDayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        request.setDayOfWeek(currentDayOfWeek);
        request.setOpensAt(LocalTime.of(8, 0));
        request.setClosesAt(LocalTime.of(17, 0));
        request.setClosed(false);

        when(restaurantRepository.findByIdAndActiveTrue(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantScheduleRepository.findByRestaurantIdAndDayOfWeek(restaurantId, (short) currentDayOfWeek))
                .thenReturn(Optional.empty());
        when(restaurantScheduleRepository.findByRestaurantIdOrderByDayOfWeek(restaurantId))
                .thenReturn(List.of(openSchedule()));

        assertThat(restaurantService.updateSchedules(restaurantId, List.of(request))).hasSize(1);

        verify(restaurantScheduleRepository).save(any(RestaurantSchedule.class));
        verify(restaurantRepository).save(restaurant);
    }

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Rita");
        user.setLastName("Owner");
        user.setEmail("rita@example.com");
        user.setActive(true);
        return user;
    }

    private Restaurant restaurant(String name, boolean open, boolean active) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        restaurant.setOwner(user());
        restaurant.setName(name);
        restaurant.setDescription("Comida salvadorena");
        restaurant.setPhone("2222-3333");
        restaurant.setEmail("restaurante@example.com");
        restaurant.setStreetAddress("Boulevard Los Proceres");
        restaurant.setCity("San Salvador");
        restaurant.setState("San Salvador");
        restaurant.setCountry("El Salvador");
        restaurant.setLocation(RestaurantMapper.toLocation(13.6929, -89.2182));
        restaurant.setOpen(open);
        restaurant.setActive(active);
        restaurant.setCreatedAt(LocalDateTime.of(2026, 5, 17, 10, 0));
        return restaurant;
    }

    private RestaurantSchedule openSchedule() {
        RestaurantSchedule schedule = new RestaurantSchedule();
        schedule.setId(1L);
        schedule.setDayOfWeek((short) LocalDateTime.now().getDayOfWeek().getValue());
        schedule.setOpensAt(LocalTime.MIN);
        schedule.setClosesAt(LocalTime.MAX);
        schedule.setClosed(false);
        return schedule;
    }

    private void applyLocationFields(RestaurantCreateDTO request) {
        request.setDescription("Comida salvadorena");
        request.setPhone("2222-3333");
        request.setEmail("restaurante@example.com");
        request.setStreetAddress("Boulevard Los Proceres");
        request.setCity("San Salvador");
        request.setState("San Salvador");
        request.setCountry("El Salvador");
        request.setLatitude(13.6929);
        request.setLongitude(-89.2182);
    }

    private void applyLocationFields(RestaurantUpdateDTO request) {
        request.setDescription("Comida salvadorena");
        request.setPhone("2222-3333");
        request.setEmail("restaurante@example.com");
        request.setStreetAddress("Boulevard Los Proceres");
        request.setCity("San Salvador");
        request.setState("San Salvador");
        request.setCountry("El Salvador");
        request.setLatitude(13.6929);
        request.setLongitude(-89.2182);
    }
}
