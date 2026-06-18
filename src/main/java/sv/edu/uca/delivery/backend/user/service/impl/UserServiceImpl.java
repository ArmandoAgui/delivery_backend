package sv.edu.uca.delivery.backend.user.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.auth.repository.RoleRepository;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.dto.request.RegisterRequest;
import sv.edu.uca.delivery.backend.user.dto.request.UpdateUserRequest;
import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.exception.UserNotFoundException;
import sv.edu.uca.delivery.backend.user.mapper.UserMapper;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;
import sv.edu.uca.delivery.backend.user.service.UserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {

        User user = userRepository.findByIdWithRole(id)
                .orElseThrow(UserNotFoundException::new);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse create(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email is already registered");
        }
        String phone = normalizeOptional(request.getPhone());
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Phone is already registered");
        }
        User user = new User();
        apply(user, request, phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByName(request.getRole() == null ? RoleName.CUSTOMER : request.getRole())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Role does not exist")));
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findByIdWithRole(id).orElseThrow(UserNotFoundException::new);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.getEmail(), id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email is already registered");
        }
        String phone = normalizeOptional(request.getPhone());
        if (phone != null && userRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Phone is already registered");
        }
        apply(user, request, phone);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Role does not exist")));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID id, UpdateUserRequest request) {
        request.setRole(null);
        return update(id, request);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        if (authenticatedUserProvider.getCurrentUserId().equals(id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Admin users cannot deactivate their own account");
        }
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse activate(UUID id) {
        User user = userRepository.findByIdWithRole(id).orElseThrow(UserNotFoundException::new);
        user.setActive(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    private void apply(User user, RegisterRequest request, String phone) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPhone(phone);
    }

    private void apply(User user, UpdateUserRequest request, String phone) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPhone(phone);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
