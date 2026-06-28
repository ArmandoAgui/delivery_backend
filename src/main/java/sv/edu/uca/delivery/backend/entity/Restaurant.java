package sv.edu.uca.delivery.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import sv.edu.uca.delivery.backend.entity.User;
import sv.edu.uca.delivery.backend.util.UuidV7Generator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @Column(nullable = false, length = 120)
    private String department;

    @Column(nullable = false, columnDefinition = "GEOGRAPHY(Point, 4326)")
    private Point location;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_open", nullable = false)
    private boolean open;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

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
