package sv.edu.uca.delivery.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndActiveTrue(UUID id);

    Optional<User> findByIdAndActiveTrueAndRoleName(UUID id, RoleName roleName);
}
