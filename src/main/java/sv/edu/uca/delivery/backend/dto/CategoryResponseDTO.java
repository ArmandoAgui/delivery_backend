package sv.edu.uca.delivery.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CategoryResponseDTO {

    private Long id;

    private UUID restaurantId;

    private String name;

    private String description;

    private boolean active;

    private LocalDateTime createdAt;
}