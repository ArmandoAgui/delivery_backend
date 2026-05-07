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
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.util.uuid.UuidV7Generator;

@Getter
@Setter
@Entity
@Table(name = "delivery_assignments")
public class DeliveryAssignment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_user_id", nullable = false)
    private User deliveryUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DeliveryStatus status;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UuidV7Generator.next();
        }
        if (status == null) {
            status = DeliveryStatus.ASSIGNED;
        }
        if (assignedAt == null) {
            assignedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }
}
