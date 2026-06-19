package sv.edu.uca.delivery.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.delivery.exception.DeliveryUnauthorizedException;
import sv.edu.uca.delivery.backend.security.principal.AppUserPrincipal;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserProvider {

    private static final String DEV_USER_ID_HEADER = "X-Dev-User-Id";

    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @Value("${app.security.dev-user-id:018f0000-0000-7000-8000-000000000003}")
    private UUID devUserId;

    public UUID getCurrentUserId() {
        String headerUserId = request.getHeader(DEV_USER_ID_HEADER);
        if (headerUserId != null && !headerUserId.isBlank()) {
            return UUID.fromString(headerUserId);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return devUserId;
        }

        if (authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal.id();
        }

        String identity = resolveIdentity(authentication);
        try {
            return UUID.fromString(identity);
        } catch (IllegalArgumentException ignored) {
            return userRepository.findByEmail(identity)
                    .orElseThrow(() -> new DeliveryUnauthorizedException("Authenticated user does not exist"))
                    .getId();
        }
    }

    private String resolveIdentity(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String value) {
            return value;
        }
        return authentication.getName();
    }
}
