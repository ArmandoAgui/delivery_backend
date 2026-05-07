package sv.edu.uca.delivery.backend.delivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.util.uuid.UuidV7Generator;

@Getter
@Setter
@Entity
@Table(name = "delivery_batches")
public class DeliveryBatch {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_user_id", nullable = false)
    private User deliveryUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private BatchStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UuidV7Generator.next();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
