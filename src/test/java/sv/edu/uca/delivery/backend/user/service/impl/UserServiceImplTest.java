package sv.edu.uca.delivery.backend.user.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sv.edu.uca.delivery.backend.auth.entity.Role;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.auth.repository.RoleRepository;
import sv.edu.uca.delivery.backend.user.dto.request.UpdateUserRequest;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.mapper.UserMapper;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void updateWithoutPasswordKeepsExistingPasswordHash() {
        UUID userId = UUID.randomUUID();
        Role role = new Role();
        role.setName(RoleName.CUSTOMER);
        User user = new User();
        user.setId(userId);
        user.setRole(role);
        user.setPasswordHash("existing-hash");
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setEmail("old@example.com");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("New");
        request.setLastName("Name");
        request.setEmail("new@example.com");
        request.setPhone("70000000");

        when(userRepository.findByIdWithRole(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(null);

        userService.update(userId, request);

        assertThat(user.getPasswordHash()).isEqualTo("existing-hash");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        verify(passwordEncoder, never()).encode(any());
    }
}
