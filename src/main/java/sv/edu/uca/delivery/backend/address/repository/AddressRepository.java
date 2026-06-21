package sv.edu.uca.delivery.backend.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.address.entity.Address;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para operaciones de persistencia
 * relacionadas con direcciones.
 */
public interface AddressRepository
        extends JpaRepository<Address, UUID> {

    /**
     * Obtiene todas las direcciones
     * asociadas a un usuario.
     */
    List<Address> findByUserId(UUID userId);
}