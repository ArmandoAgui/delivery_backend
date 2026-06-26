package sv.edu.uca.delivery.backend.mapper;

import sv.edu.uca.delivery.backend.dto.PromotionResponseDTO;
import sv.edu.uca.delivery.backend.entity.Promotion;

import java.time.LocalDate;

public class PromotionMapper {

    public static PromotionResponseDTO toDTO(Promotion promotion) {

        LocalDate today = LocalDate.now();

        boolean currentlyActive =
                promotion.isActive()
                        && !today.isBefore(promotion.getStartDate())
                        && !today.isAfter(promotion.getEndDate());

        return PromotionResponseDTO.builder()
                .id(promotion.getId())
                .restaurantId(promotion.getRestaurant().getId())
                .restaurantName(promotion.getRestaurant().getName())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .discountPercentage(promotion.getDiscountPercentage())
                .active(promotion.isActive())
                .currentlyActive(currentlyActive)
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .createdAt(promotion.getCreatedAt())
                .build();
    }
}