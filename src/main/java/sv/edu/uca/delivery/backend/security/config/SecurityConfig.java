package sv.edu.uca.delivery.backend.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sv.edu.uca.delivery.backend.security.filter.JwtAuthenticationFilter;
import sv.edu.uca.delivery.backend.security.handler.JsonAccessDeniedHandler;
import sv.edu.uca.delivery.backend.security.handler.JsonAuthenticationEntryPoint;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/restaurants/**", "/api/restaurants/**",
                                "/products/**", "/api/products/**",
                                "/categories/**", "/api/categories/**",
                                "/promotions/**", "/api/promotions/**").permitAll()
                        .requestMatchers("/api/admin/**", "/api/reports/**", "/api/coupons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/restaurants/**", "/api/restaurants/**",
                                "/products/**", "/api/products/**",
                                "/categories/**", "/api/categories/**",
                                "/promotions/**", "/api/promotions/**").hasAnyRole("ADMIN", "RESTAURANT")
                        .requestMatchers(HttpMethod.PUT,
                                "/restaurants/**", "/api/restaurants/**",
                                "/products/**", "/api/products/**",
                                "/categories/**", "/api/categories/**",
                                "/promotions/**", "/api/promotions/**").hasAnyRole("ADMIN", "RESTAURANT")
                        .requestMatchers(HttpMethod.PATCH,
                                "/restaurants/**", "/api/restaurants/**",
                                "/products/**", "/api/products/**",
                                "/categories/**", "/api/categories/**",
                                "/promotions/**", "/api/promotions/**").hasAnyRole("ADMIN", "RESTAURANT")
                        .requestMatchers(HttpMethod.POST, "/api/deliveries/assign").hasRole("ADMIN")
                        .requestMatchers("/api/deliveries/**").hasAnyRole("ADMIN", "DELIVERY")
                        .requestMatchers("/api/orders/restaurant/**", "/api/orders/*/confirm", "/api/orders/*/reject")
                                .hasAnyRole("ADMIN", "RESTAURANT")
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/cart/**", "/api/orders/**", "/api/complaints/**",
                                "/api/loyalty/**", "/api/reviews/**", "/api/auth/me").authenticated()
                        .anyRequest().authenticated()
                );
        jwtAuthenticationFilter.ifAvailable(filter -> http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
