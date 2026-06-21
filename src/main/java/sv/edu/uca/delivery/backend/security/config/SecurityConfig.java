package sv.edu.uca.delivery.backend.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sv.edu.uca.delivery.backend.security.filter.JwtAuthenticationFilter;
import sv.edu.uca.delivery.backend.security.handler.CustomAuthenticationEntryPoint;

/**
 * Configuración principal de Spring Security.
 * Define rutas públicas, protegidas y filtros JWT.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final CustomAuthenticationEntryPoint entryPoint;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Rutas públicas de autenticación
                        .requestMatchers("/api/auth/**").permitAll()

                        // Acceso exclusivo para administradores
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Acceso para restaurante
                        .requestMatchers("/api/restaurants/**").hasRole("RESTAURANT")

                        // Acceso para delivery
                        .requestMatchers("/api/delivery/**").hasRole("DELIVERY")

                        // Todas las demás rutas requieren autenticación
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                entryPoint
                        )
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .httpBasic(Customizer.withDefaults())

                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}