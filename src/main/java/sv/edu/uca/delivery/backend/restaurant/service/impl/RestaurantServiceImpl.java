package sv.edu.uca.delivery.backend.restaurant.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.common.time.AppClock;
import sv.edu.uca.delivery.backend.media.service.ImageStorageService;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantCreateDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantScheduleDTO;
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
import sv.edu.uca.delivery.backend.restaurant.service.RestaurantService;
import sv.edu.uca.delivery.backend.review.repository.ReviewRepository;
import sv.edu.uca.delivery.backend.security.AccessControlService;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private static final String DEFAULT_COUNTRY = "El Salvador";

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final RestaurantScheduleRepository restaurantScheduleRepository;
    private final AccessControlService accessControlService;
    private final ImageStorageService imageStorageService;
    private final ReviewRepository reviewRepository;

    @Autowired
    public RestaurantServiceImpl(
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            RestaurantScheduleRepository restaurantScheduleRepository,
            AccessControlService accessControlService,
            ImageStorageService imageStorageService,
            ReviewRepository reviewRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.restaurantScheduleRepository = restaurantScheduleRepository;
        this.accessControlService = accessControlService;
        this.imageStorageService = imageStorageService;
        this.reviewRepository = reviewRepository;
    }

    public RestaurantServiceImpl(
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            RestaurantScheduleRepository restaurantScheduleRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.restaurantScheduleRepository = restaurantScheduleRepository;
        this.accessControlService = null;
        this.imageStorageService = null;
        this.reviewRepository = null;
    }

    @Override
    @Transactional
    public RestaurantResponseDTO create(RestaurantCreateDTO dto) {

        User owner = userRepository.findActiveUserByIdAndRole(dto.getOwnerId(), RoleName.RESTAURANT)
                .orElseThrow(RestaurantOwnerNotFoundException::new);
        if (accessControlService != null) {
            User current = accessControlService.currentUser();
            if (current.getRole().getName() != RoleName.ADMIN && !current.getId().equals(owner.getId())) {
                accessControlService.requireAdmin();
            }
        }

        if (restaurantRepository.existsByOwnerId(owner.getId())) {
            throw new RestaurantOwnerAlreadyHasRestaurantException();
        }

        Restaurant restaurant = new Restaurant();

        restaurant.setOwner(owner);
        applyCreateFields(restaurant, dto);
        restaurant.setOpen(false);

        restaurantRepository.save(restaurant);

        return toResponse(restaurant, isCurrentlyOpen(restaurant.getId(), AppClock.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> findAll() {
        LocalDateTime now = AppClock.now();

        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(restaurant -> toResponse(restaurant, isCurrentlyOpen(restaurant.getId(), now)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponseDTO findMine() {
        if (accessControlService == null) {
            throw new RestaurantNotFoundException();
        }
        User current = accessControlService.currentUser();
        if (current.getRole().getName() != RoleName.RESTAURANT) {
            accessControlService.requireAdmin();
        }
        Restaurant restaurant = restaurantRepository.findByOwnerIdAndActiveTrue(current.getId())
                .orElseThrow(RestaurantNotFoundException::new);
        return toResponse(restaurant, isCurrentlyOpen(restaurant.getId(), AppClock.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponseDTO findById(UUID id) {

        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(RestaurantNotFoundException::new);

        return toResponse(restaurant, isCurrentlyOpen(restaurant.getId(), AppClock.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        LocalDateTime now = AppClock.now();
        return restaurantRepository.searchActive(query.trim())
                .stream()
                .map(restaurant -> toResponse(restaurant, isCurrentlyOpen(restaurant.getId(), now)))
                .toList();
    }

    @Override
    @Transactional
    public RestaurantResponseDTO update(UUID id, RestaurantUpdateDTO dto) {

        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(RestaurantNotFoundException::new);
        requireOwner(restaurant);

        applyUpdateFields(restaurant, dto);

        restaurantRepository.save(restaurant);

        return toResponse(restaurant, isCurrentlyOpen(restaurant.getId(), AppClock.now()));
    }

    @Override
    @Transactional
    public RestaurantResponseDTO uploadImage(UUID id, org.springframework.web.multipart.MultipartFile file) {
        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(RestaurantNotFoundException::new);
        requireOwner(restaurant);
        if (imageStorageService == null) {
            throw new RestaurantNotFoundException();
        }
        restaurant.setImageUrl(imageStorageService.storeRestaurantImage(restaurant.getId(), file, restaurant.getImageUrl()));
        restaurantRepository.save(restaurant);
        return toResponse(restaurant, isCurrentlyOpen(restaurant.getId(), AppClock.now()));
    }

    @Override
    @Transactional
    public void deleteImage(UUID id) {
        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(RestaurantNotFoundException::new);
        requireOwner(restaurant);
        if (imageStorageService != null) {
            imageStorageService.delete(restaurant.getImageUrl());
        }
        restaurant.setImageUrl(null);
        restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {

        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(RestaurantNotFoundException::new);
        requireOwner(restaurant);

        restaurant.setActive(false);
        if (imageStorageService != null) {
            imageStorageService.delete(restaurant.getImageUrl());
        }
        restaurant.setImageUrl(null);

        restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> findOpenRestaurants() {
        LocalDateTime now = AppClock.now();

        return restaurantRepository.findByActiveTrue()
                .stream()
                .filter(restaurant -> isCurrentlyOpen(restaurant.getId(), now))
                .map(restaurant -> toResponse(restaurant, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> findNearby(double latitude, double longitude, double radiusKm) {
        LocalDateTime now = AppClock.now();
        double safeRadiusMeters = Math.max(0.5, Math.min(radiusKm, 50.0)) * 1000.0;
        return restaurantRepository.findNearby(latitude, longitude, safeRadiusMeters)
                .stream()
                .map(restaurant -> toResponse(restaurant, isCurrentlyOpen(restaurant.getId(), now)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantScheduleDTO> findSchedules(UUID restaurantId) {
        findActiveRestaurant(restaurantId);

        return restaurantScheduleRepository.findByRestaurantIdOrderByDayOfWeek(restaurantId)
                .stream()
                .map(RestaurantMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public List<RestaurantScheduleDTO> updateSchedules(
            UUID restaurantId,
            List<RestaurantScheduleRequestDTO> schedules
    ) {
        Restaurant restaurant = findActiveRestaurant(restaurantId);
        requireOwner(restaurant);
        validateSchedules(schedules);

        schedules.forEach(request -> {
            RestaurantSchedule schedule = restaurantScheduleRepository
                    .findByRestaurantIdAndDayOfWeek(restaurantId, (short) request.getDayOfWeek())
                    .orElseGet(RestaurantSchedule::new);

            schedule.setRestaurant(restaurant);
            schedule.setDayOfWeek((short) request.getDayOfWeek());
            schedule.setClosed(request.isClosed());
            schedule.setOpensAt(request.isClosed() ? null : request.getOpensAt());
            schedule.setClosesAt(request.isClosed() ? null : request.getClosesAt());

            restaurantScheduleRepository.save(schedule);
        });

        restaurant.setOpen(isCurrentlyOpen(restaurantId, AppClock.now()));
        restaurantRepository.save(restaurant);

        return findSchedules(restaurantId);
    }

    private void applyCreateFields(Restaurant restaurant, RestaurantCreateDTO dto) {
        restaurant.setName(dto.getName());
        restaurant.setDescription(dto.getDescription());
        restaurant.setPhone(dto.getPhone());
        restaurant.setEmail(dto.getEmail());
        restaurant.setStreetAddress(dto.getStreetAddress());
        restaurant.setCity(dto.getCity());
        restaurant.setState(dto.getState());
        restaurant.setCountry(DEFAULT_COUNTRY);
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
        restaurant.setCountry(DEFAULT_COUNTRY);
        restaurant.setLocation(RestaurantMapper.toLocation(dto.getLatitude(), dto.getLongitude()));
    }

    private Restaurant findActiveRestaurant(UUID restaurantId) {
        return restaurantRepository.findByIdAndActiveTrue(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);
    }

    private RestaurantResponseDTO toResponse(Restaurant restaurant, boolean open) {
        if (reviewRepository == null) {
            return RestaurantMapper.toDTO(restaurant, open);
        }
        return RestaurantMapper.toDTO(
                restaurant,
                open,
                reviewRepository.averageRatingByRestaurantId(restaurant.getId()),
                reviewRepository.countByRestaurantId(restaurant.getId())
        );
    }

    private void requireOwner(Restaurant restaurant) {
        if (accessControlService != null) {
            accessControlService.requireAdminOrRestaurantOwner(restaurant);
        }
    }

    private boolean isCurrentlyOpen(UUID restaurantId, LocalDateTime now) {
        short dayOfWeek = (short) now.getDayOfWeek().getValue();
        LocalTime currentTime = now.toLocalTime();

        return restaurantScheduleRepository.findByRestaurantIdAndDayOfWeek(restaurantId, dayOfWeek)
                .filter(schedule -> !schedule.isClosed())
                .filter(schedule -> schedule.getOpensAt() != null && schedule.getClosesAt() != null)
                .filter(schedule -> !currentTime.isBefore(schedule.getOpensAt()))
                .filter(schedule -> currentTime.isBefore(schedule.getClosesAt()))
                .isPresent();
    }

    private void validateSchedules(List<RestaurantScheduleRequestDTO> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            throw new RestaurantScheduleInvalidException("At least one schedule is required");
        }

        Set<Integer> days = new HashSet<>();
        for (RestaurantScheduleRequestDTO schedule : schedules) {
            if (!days.add(schedule.getDayOfWeek())) {
                throw new RestaurantScheduleInvalidException("Duplicate schedule day: " + schedule.getDayOfWeek());
            }
            if (schedule.getDayOfWeek() < 1 || schedule.getDayOfWeek() > 7) {
                throw new RestaurantScheduleInvalidException("Schedule day must be between 1 and 7");
            }
            if (schedule.isClosed()) {
                continue;
            }
            if (schedule.getOpensAt() == null || schedule.getClosesAt() == null) {
                throw new RestaurantScheduleInvalidException("Open schedule days require opensAt and closesAt");
            }
            if (!schedule.getClosesAt().isAfter(schedule.getOpensAt())) {
                throw new RestaurantScheduleInvalidException("Schedule closesAt must be after opensAt");
            }
        }
    }
}
