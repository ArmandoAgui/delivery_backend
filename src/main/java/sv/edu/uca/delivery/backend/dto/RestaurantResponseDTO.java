package sv.edu.uca.delivery.backend.dto;

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

    private String description;

    private String phone;

    private String email;

    private String streetAddress;

    private String city;

    private String state;

    private String country;

    private Double latitude;

    private Double longitude;

    private String imageUrl;

    private boolean open;

    private boolean active;

    private Double averageRating;

    private long reviewCount;

    private LocalDateTime createdAt;
}
