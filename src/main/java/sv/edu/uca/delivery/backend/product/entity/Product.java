package sv.edu.uca.delivery.backend.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sv.edu.uca.delivery.backend.category.entity.Category;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.util.uuid.UuidV7Generator;
import sv.edu.uca.delivery.backend.category.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;


    // esto sustituye al ProductCategory, para que funcione el crud Category

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    // me esta generando error

    //@Enumerated(EnumType.STRING)
    //@Column(nullable = false)
    //private ProductCategory category;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UuidV7Generator.generate();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}