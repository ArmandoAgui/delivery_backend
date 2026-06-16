package sv.edu.uca.delivery.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndActiveTrue(UUID id);

    Optional<User> findByIdAndActiveTrueAndRoleName(UUID id, RoleName roleName);

    @Query("""
            select u
            from User u
            join fetch u.role r
            where u.id = :id
              and u.active = true
              and r.name = :roleName
            """)
    Optional<User> findActiveUserByIdAndRole(UUID id, RoleName roleName);
}
