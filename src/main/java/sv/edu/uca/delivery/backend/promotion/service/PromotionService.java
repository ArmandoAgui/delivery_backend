package sv.edu.uca.delivery.backend.promotion.service;

import sv.edu.uca.delivery.backend.promotion.dto.PromotionCreateDTO;
import sv.edu.uca.delivery.backend.promotion.dto.PromotionUpdateDTO;
import sv.edu.uca.delivery.backend.promotion.dto.response.PromotionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PromotionService {

    PromotionResponseDTO create(PromotionCreateDTO dto);

    List<PromotionResponseDTO> findAll();

    PromotionResponseDTO findById(Long id);

    PromotionResponseDTO update(Long id, PromotionUpdateDTO dto);

    void softDelete(Long id);

    List<PromotionResponseDTO> findByRestaurant(UUID restaurantId);

    List<PromotionResponseDTO> findActivePromotions();

    PromotionResponseDTO updateStatus(Long id, boolean active);
}