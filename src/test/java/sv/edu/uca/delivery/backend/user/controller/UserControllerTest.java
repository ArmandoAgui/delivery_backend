package sv.edu.uca.delivery.backend.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sv.edu.uca.delivery.backend.common.exception.GlobalExceptionHandler;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;
import sv.edu.uca.delivery.backend.user.service.UserService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Test
    void updateMeDoesNotRequirePassword() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = response(userId, "cliente.actualizado@example.com", "CUSTOMER");

        when(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userService.updateProfile(eq(userId), any())).thenReturn(response);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Cliente",
                                  "lastName":"Actualizado",
                                  "email":"cliente.actualizado@example.com",
                                  "phone":"70000000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("cliente.actualizado@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        verify(userService).updateProfile(eq(userId), any());
    }

    @Test
    void findAllPagedReturnsPageShape() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
                response(UUID.randomUUID(), "admin@example.com", "ADMIN"),
                response(UUID.randomUUID(), "cliente@example.com", "CUSTOMER")
        ));

        mockMvc.perform(get("/api/users/page?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(1));

        verify(userService).findAll();
    }

    private UserResponse response(UUID id, String email, String role) {
        return UserResponse.builder()
                .id(id)
                .firstName("Nombre")
                .lastName("Apellido")
                .email(email)
                .phone("70000000")
                .role(role)
                .build();
    }
}
