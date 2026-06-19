package sv.edu.uca.delivery.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User currentUser() {
        return userRepository.findByIdWithRole(authenticatedUserProvider.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Authenticated user does not exist"));
    }

    public void requireAdmin() {
        if (currentUser().getRole().getName() != RoleName.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Admin role is required");
        }
    }

    public void requireAdminOrRestaurantOwner(Restaurant restaurant) {
        User current = currentUser();
        if (current.getRole().getName() == RoleName.ADMIN) {
            return;
        }
        if (current.getRole().getName() == RoleName.RESTAURANT
                && restaurant.getOwner().getId().equals(current.getId())) {
            return;
        }
        throw new BusinessException(HttpStatus.FORBIDDEN, "Restaurant owner or admin role is required");
    }
}
