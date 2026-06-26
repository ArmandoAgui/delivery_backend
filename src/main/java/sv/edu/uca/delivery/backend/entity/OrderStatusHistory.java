package sv.edu.uca.delivery.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import sv.edu.uca.delivery.backend.entity.User;
import sv.edu.uca.delivery.backend.util.UuidV7Generator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private OrderStatus newStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant changedAt;

    @Column(length = 500)
    private String notes;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UuidV7Generator.generate();
        }
    }
}
