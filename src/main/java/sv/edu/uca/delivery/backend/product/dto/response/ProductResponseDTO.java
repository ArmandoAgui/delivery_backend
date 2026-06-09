package sv.edu.uca.delivery.backend.product.dto.response;

import lombok.Builder;
import lombok.Getter;
import sv.edu.uca.delivery.backend.product.entity.ProductCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProductResponseDTO {

    private UUID id;

    private UUID restaurantId;

    private String name;

    private String description;

    private BigDecimal price;

    //private ProductCategory category;

    private boolean available;

    //private boolean active;

    private LocalDateTime createdAt;
}