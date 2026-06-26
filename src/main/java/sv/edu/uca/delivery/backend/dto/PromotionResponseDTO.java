package sv.edu.uca.delivery.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PromotionResponseDTO {

    private Long id;

    private UUID restaurantId;

    private String restaurantName;

    private String title;

    private String description;

    private Integer discountPercentage;

    private boolean active;

    private boolean currentlyActive;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime createdAt;

}