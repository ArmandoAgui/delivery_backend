package sv.edu.uca.delivery.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DevSeedRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean enabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        String password = passwordEncoder.encode("Password123!");
        jdbcTemplate.update("""
                insert into roles (id, name, description)
                values
                  (1, 'ADMIN', 'Administrator'),
                  (2, 'CUSTOMER', 'Customer'),
                  (3, 'RESTAURANT', 'Restaurant owner'),
                  (4, 'DELIVERY', 'Delivery driver')
                on conflict (name) do nothing
                """);
        seedUser("018f9000-0000-7000-8000-000000000001", 1, "Admin", "Dev", "admin.dev@example.com", password);
        seedUser("018f9000-0000-7000-8000-000000000002", 2, "Cliente", "Dev", "cliente.dev@example.com", password);
        seedUser("018f9000-0000-7000-8000-000000000003", 3, "Restaurante", "Dev", "restaurante.dev@example.com", password);
        seedUser("018f9000-0000-7000-8000-000000000004", 4, "Repartidor", "Dev", "repartidor.dev@example.com", password);
        seedUser("018f0000-0000-7000-8000-000000000003", 4, "Repartidor", "Cercano", "repartidor.cercano.dev@example.com", password);
        seedDeliveryProfile("018f9000-0000-7000-8000-000000000004", -89.2300, 13.7000);
        seedDeliveryProfile("018f0000-0000-7000-8000-000000000003", -89.2205, 13.6905);

        jdbcTemplate.update("""
                insert into addresses (id, user_id, label, street_address, city, state, location, is_default)
                values (cast(? as uuid), cast(? as uuid), 'Casa', 'Calle Dev 123', 'San Salvador', 'San Salvador',
                        ST_SetSRID(ST_MakePoint(-89.2182, 13.6929), 4326)::geography, true)
                on conflict (id) do nothing
                """, "018f9000-0000-7000-8000-000000000101", "018f9000-0000-7000-8000-000000000002");
        jdbcTemplate.update("""
                insert into restaurants (id, owner_user_id, name, description, phone, email, street_address, city, state, country, location, is_open, is_active)
                values (cast(? as uuid), cast(? as uuid), 'Restaurante Dev', 'Seed para pruebas end-to-end', '+50370000000', 'restaurante.dev.seed@example.com',
                        'Boulevard Dev 500', 'San Salvador', 'San Salvador', 'El Salvador',
                        ST_SetSRID(ST_MakePoint(-89.2200, 13.6900), 4326)::geography, true, true)
                on conflict (id) do nothing
                """, "018f9000-0000-7000-8000-000000000201", "018f9000-0000-7000-8000-000000000003");
        jdbcTemplate.update("""
                insert into categories (restaurant_id, name, description)
                select cast(? as uuid), 'Combos Dev', 'Categoria seed'
                where not exists (select 1 from categories where restaurant_id = cast(? as uuid) and name = 'Combos Dev')
                """, "018f9000-0000-7000-8000-000000000201", "018f9000-0000-7000-8000-000000000201");
        Long categoryId = jdbcTemplate.queryForObject("""
                select id from categories where restaurant_id = cast(? as uuid) and name = 'Combos Dev'
                """, Long.class, "018f9000-0000-7000-8000-000000000201");
        jdbcTemplate.update("""
                insert into products (id, restaurant_id, category_id, name, description, price, is_available)
                values (cast(? as uuid), cast(? as uuid), ?, 'Combo Dev', 'Producto seed', 8.50, true)
                on conflict (id) do nothing
                """, "018f9000-0000-7000-8000-000000000301", "018f9000-0000-7000-8000-000000000201", categoryId);
        jdbcTemplate.update("""
                insert into coupons (code, description, discount_type, discount_value, minimum_order_amount, max_discount_amount, usage_limit, starts_at, expires_at, is_active)
                values ('DEV10', '10% seed dev', 'PERCENTAGE', 10, 0, 5, 1000, now() - interval '1 day', now() + interval '30 days', true)
                on conflict (code) do nothing
                """);
    }

    private void seedUser(String id, long roleId, String firstName, String lastName, String email, String password) {
        jdbcTemplate.update("""
                insert into users (id, role_id, first_name, last_name, email, phone, password_hash, is_active)
                values (cast(? as uuid), ?, ?, ?, ?, null, ?, true)
                on conflict (id) do update set
                    role_id = excluded.role_id,
                    first_name = excluded.first_name,
                    last_name = excluded.last_name,
                    email = excluded.email,
                    password_hash = excluded.password_hash,
                    is_active = true
                """, id, roleId, firstName, lastName, email, password);
    }

    private void seedDeliveryProfile(String deliveryUserId, double longitude, double latitude) {
        jdbcTemplate.update("""
                insert into delivery_profiles (delivery_user_id, is_available, updated_at)
                values (cast(? as uuid), true, now())
                on conflict (delivery_user_id) do update set is_available = true, updated_at = now()
                """, deliveryUserId);
        jdbcTemplate.update("""
                insert into delivery_locations (id, delivery_user_id, location, recorded_at, created_at)
                select cast(? as uuid), cast(? as uuid), ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, now(), now()
                where not exists (
                    select 1 from delivery_locations
                    where delivery_user_id = cast(? as uuid)
                )
                """, UUID.randomUUID(), deliveryUserId, longitude, latitude, deliveryUserId);
    }
}
