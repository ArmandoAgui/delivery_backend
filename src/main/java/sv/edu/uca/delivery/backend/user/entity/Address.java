package sv.edu.uca.delivery.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import sv.edu.uca.delivery.backend.util.uuid.UuidV7Generator;

@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    private UUID id;

    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @Column(nullable = false)
    private String city;

    private String state;

    @Column(nullable = false)
    private String country;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UuidV7Generator.next();
        }
    }
}
