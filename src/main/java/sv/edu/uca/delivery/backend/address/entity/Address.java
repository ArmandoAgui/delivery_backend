package sv.edu.uca.delivery.backend.address.entity;

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
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.util.uuid.UuidV7Generator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una dirección registrada
 * por un usuario dentro de la plataforma.
 *
 * Un usuario puede almacenar múltiples direcciones
 * para facilitar futuras órdenes y entregas.
 */
@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address {

    // Identificador único de la dirección
    @Id
    private UUID id;

    // Usuario propietario de la dirección
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Nombre descriptivo para identificar la dirección
    @Column(length = 80)
    private String label;

    // Dirección exacta de entrega
    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    // Ciudad asociada a la dirección
    @Column(nullable = false, length = 120)
    private String city;

    @Column(length = 120)
    private String state;

    @Column(nullable = false, length = 120)
    private String country;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    // Indica si esta dirección es la predeterminada
    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Genera automáticamente los valores necesarios
     * antes de persistir la entidad.
     */
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
