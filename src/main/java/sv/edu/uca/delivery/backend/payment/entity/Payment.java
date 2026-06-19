package sv.edu.uca.delivery.backend.payment.entity;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.util.uuid.UuidV7Generator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 40)
    private String provider = "SIMULATED";

    @Column(name = "provider_transaction_id", length = 150)
    private String providerTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal amount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

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
