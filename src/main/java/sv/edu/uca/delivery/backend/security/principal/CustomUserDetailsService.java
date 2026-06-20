package sv.edu.uca.delivery.backend.security.principal;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        return new CustomUserDetails(user);
    }
}