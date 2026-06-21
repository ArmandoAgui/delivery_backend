package sv.edu.uca.delivery.backend.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.exception.UserNotFoundException;
import sv.edu.uca.delivery.backend.user.mapper.UserMapper;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;
import sv.edu.uca.delivery.backend.user.service.UserService;

import java.util.UUID;

/**
 * Implementación de la lógica de negocio relacionada
 * con la gestión de usuarios.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public UserResponse findById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        return userMapper.toResponse(user);
    }


}