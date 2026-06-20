package sv.edu.uca.delivery.backend.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.address.entity.Address;

import java.util.List;
import java.util.UUID;

public interface AddressRepository
        extends JpaRepository<Address, UUID> {

    List<Address> findByUserId(UUID userId);
}