package sv.edu.uca.delivery.backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.auth.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenAndRevokedAtIsNull(String token);
}
