package sv.edu.uca.delivery.backend.restaurant.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RestaurantResponseDTO {

    private UUID id;

    private UUID ownerId;

    private String name;

    private boolean open;

    private boolean active;

    private LocalDateTime createdAt;
}