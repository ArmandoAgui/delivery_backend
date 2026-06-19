package sv.edu.uca.delivery.backend.security.principal;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sv.edu.uca.delivery.backend.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record AppUserPrincipal(
        UUID id,
        String email,
        String password,
        String role,
        boolean active
) implements UserDetails {

    public static AppUserPrincipal from(User user) {
        return new AppUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().getName().name(),
                user.isActive()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
