package sv.edu.uca.delivery.backend.promotion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.promotion.dto.PromotionCreateDTO;
import sv.edu.uca.delivery.backend.promotion.dto.PromotionUpdateDTO;
import sv.edu.uca.delivery.backend.promotion.dto.response.PromotionResponseDTO;
import sv.edu.uca.delivery.backend.promotion.entity.Promotion;
import sv.edu.uca.delivery.backend.promotion.exception.PromotionAlreadyExistsException;
import sv.edu.uca.delivery.backend.promotion.exception.PromotionDateInvalidException;
import sv.edu.uca.delivery.backend.promotion.exception.PromotionNotFoundException;
import sv.edu.uca.delivery.backend.promotion.mapper.PromotionMapper;
import sv.edu.uca.delivery.backend.promotion.repository.PromotionRepository;
import sv.edu.uca.delivery.backend.promotion.service.PromotionService;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.restaurant.exception.RestaurantNotFoundException;
import sv.edu.uca.delivery.backend.restaurant.repository.RestaurantRepository;
import sv.edu.uca.delivery.backend.security.AccessControlService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final RestaurantRepository restaurantRepository;
    private final AccessControlService accessControlService;

    @Override
    @Transactional
    public PromotionResponseDTO create(PromotionCreateDTO dto) {

        Restaurant restaurant = restaurantRepository
                .findByIdAndActiveTrue(dto.getRestaurantId())
                .orElseThrow(RestaurantNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(restaurant);

        validateDates(dto.getStartDate(), dto.getEndDate());

        if (promotionRepository.existsByRestaurant_IdAndActiveTrue(dto.getRestaurantId())) {
            throw new PromotionAlreadyExistsException();
        }

        Promotion promotion = new Promotion();

        promotion.setRestaurant(restaurant);
        promotion.setTitle(dto.getTitle());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountPercentage(dto.getDiscountPercentage());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        promotion.setActive(true);

        promotionRepository.save(promotion);

        return PromotionMapper.toDTO(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponseDTO> findAll() {

        return promotionRepository.findAll()
                .stream()
                .map(PromotionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponseDTO findById(Long id) {

        Promotion promotion = promotionRepository
                .findById(id)
                .orElseThrow(PromotionNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(promotion.getRestaurant());

        return PromotionMapper.toDTO(promotion);
    }

    @Override
    @Transactional
    public PromotionResponseDTO update(Long id, PromotionUpdateDTO dto) {

        Promotion promotion = promotionRepository
                .findById(id)
                .orElseThrow(PromotionNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(promotion.getRestaurant());

        validateDates(dto.getStartDate(), dto.getEndDate());

        promotion.setTitle(dto.getTitle());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountPercentage(dto.getDiscountPercentage());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());

        promotionRepository.save(promotion);

        return PromotionMapper.toDTO(promotion);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {

        Promotion promotion = promotionRepository
                .findById(id)
                .orElseThrow(PromotionNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(promotion.getRestaurant());

        promotion.setActive(false);

        promotionRepository.save(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponseDTO> findByRestaurant(UUID restaurantId) {

        return promotionRepository
                .findByRestaurant_Id(restaurantId)
                .stream()
                .map(PromotionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponseDTO> findActivePromotions() {

        LocalDate today = LocalDate.now();

        return promotionRepository.findByActiveTrue()
                .stream()
                .filter(p -> !today.isBefore(p.getStartDate()))
                .filter(p -> !today.isAfter(p.getEndDate()))
                .map(PromotionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public PromotionResponseDTO updateStatus(Long id, boolean active) {

        Promotion promotion = promotionRepository
                .findById(id)
                .orElseThrow(PromotionNotFoundException::new);

        if (active &&
                promotionRepository.existsByRestaurant_IdAndActiveTrue(
                        promotion.getRestaurant().getId()
                ) &&
                !promotion.isActive()) {

            throw new PromotionAlreadyExistsException();
        }

        promotion.setActive(active);

        promotionRepository.save(promotion);

        return PromotionMapper.toDTO(promotion);
    }

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (endDate.isBefore(startDate)) {
            throw new PromotionDateInvalidException(
                    "End date must be after start date"
            );
        }
    }
}
