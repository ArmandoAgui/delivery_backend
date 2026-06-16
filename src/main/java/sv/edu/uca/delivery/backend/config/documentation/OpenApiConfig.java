package sv.edu.uca.delivery.backend.config.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI deliveryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Delivery Backend API")
                        .version("1.0.0")
                        .description("""
                                API REST para un servicio de delivery de comida.

                                Roles soportados:
                                - ADMIN: administra usuarios, reclamos, cupones, reportes y comisiones.
                                - CUSTOMER: gestiona direcciones, carrito, pedidos, reclamos, fidelidad y reviews.
                                - RESTAURANT: administra restaurante, menu, categorias, promociones y pedidos propios.
                                - DELIVERY: consulta entregas asignadas y actualiza estados de delivery.

                                Autenticacion:
                                1. Ejecuta POST /api/auth/login.
                                2. Copia el accessToken.
                                3. Presiona Authorize y usa: Bearer <token>.
                                """)
                        .contact(new Contact()
                                .name("Equipo Delivery UCA")
                                .email("delivery-backend@example.com"))
                        .license(new License()
                                .name("Academic project")
                                .url("https://uca.edu.sv")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local default"),
                        new Server().url("http://localhost:8081").description("Local alternate/testing")
                ))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token returned by /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .tags(List.of(
                        new Tag().name("Auth").description("Registro, login, refresh token, logout y perfil autenticado."),
                        new Tag().name("Users").description("CRUD administrativo de usuarios y perfil autenticado."),
                        new Tag().name("Addresses").description("Direcciones del cliente autenticado."),
                        new Tag().name("Restaurants").description("Restaurantes, horarios, estado y busquedas publicas."),
                        new Tag().name("Categories").description("Categorias de menu por restaurante."),
                        new Tag().name("Products").description("Productos/menu, precios, disponibilidad y categorias."),
                        new Tag().name("Promotions").description("Promociones del catalogo por restaurante."),
                        new Tag().name("Cart").description("Carrito activo del cliente y calculo de subtotal."),
                        new Tag().name("Orders").description("Pedidos desde carrito, estados, historial y tracking REST."),
                        new Tag().name("Deliveries").description("Asignacion de repartidor y cambio de estados de entrega."),
                        new Tag().name("Complaints").description("Reclamos, resolucion administrativa y reembolsos simples."),
                        new Tag().name("Coupons").description("Cupones, vigencia, limites y activacion administrativa."),
                        new Tag().name("Loyalty").description("Puntos de fidelidad, acumulacion y canje basico."),
                        new Tag().name("Reviews").description("Calificaciones de pedidos entregados."),
                        new Tag().name("Reports").description("Reportes administrativos y estadisticas basicas."),
                        new Tag().name("Admin").description("Configuracion administrativa, como comisiones.")
                ));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01 Auth & Users")
                .pathsToMatch("/api/auth/**", "/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi catalogApi() {
        return GroupedOpenApi.builder()
                .group("02 Catalogo")
                .pathsToMatch(
                        "/api/restaurants/**",
                        "/api/categories/**",
                        "/api/products/**",
                        "/api/promotions/**",
                        "/restaurants/**",
                        "/categories/**",
                        "/products/**",
                        "/promotions/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi commerceApi() {
        return GroupedOpenApi.builder()
                .group("03 Carrito y Pedidos")
                .pathsToMatch("/api/cart/**", "/api/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi operationsApi() {
        return GroupedOpenApi.builder()
                .group("04 Delivery, Reclamos y Reviews")
                .pathsToMatch("/api/deliveries/**", "/api/complaints/**", "/api/reviews/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("05 Admin, Cupones, Fidelidad y Reportes")
                .pathsToMatch("/api/admin/**", "/api/coupons/**", "/api/loyalty/**", "/api/reports/**")
                .build();
    }
}
