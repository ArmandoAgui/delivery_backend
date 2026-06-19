package sv.edu.uca.delivery.backend.complaint.entity;

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
import lombok.Getter;
import lombok.Setter;
import sv.edu.uca.delivery.backend.payment.entity.Payment;
import sv.edu.uca.delivery.backend.util.uuid.UuidV7Generator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RefundStatus status = RefundStatus.REQUESTED;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

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
