# Backend Integration Report

Fecha: 2026-06-16
Rama: `feature/backend-integration-delivery`

## 1. Ramas revisadas

Trabajo realizado sobre la rama actual, sin cambiar de rama. La integración tomó como base el estado ya consolidado en `feature/backend-integration-delivery`, que venía de `develop` y de los módulos traídos desde ramas funcionales:

- `feature/crud-product`
- `feature/user-module`
- `feature/order-module`
- `feature/crud-restaurant`
- `feature/complaint-module`
- `feature/delivery-module`
- `develop`

## 2. Módulos integrados

- Auth/Security con JWT, refresh token, BCrypt, filtro Bearer, handlers JSON para `401` y `403`.
- Usuarios con CRUD administrativo, perfil autenticado y validación de rol activo.
- Direcciones del usuario autenticado.
- Restaurantes con aliases `/api`, horarios y autorización por dueño/admin.
- Categorías, productos y promociones con aliases `/api` y validación de pertenencia restaurante-categoría.
- Carrito por cliente, items, subtotal y bloqueo de mezcla de restaurantes.
- Pedidos desde carrito, items snapshot, totales, cupón, historial y tracking REST.
- Delivery con asignación automática simple, consulta por repartidor y transición de estados.
- Reclamos con handlers globales y validación de usuario activo.
- Cupones administrativos y aplicación al pedido.
- Fidelidad con acumulación de puntos al entregar pedido y canje básico.
- Calificaciones para restaurante/repartidor sobre pedidos entregados, evitando duplicados.
- Reportes administrativos mínimos.
- Administración básica de comisiones.

## 3. Código creado desde cero

- `auth`: login, registro, refresh, logout, entidad `RefreshToken`, repositorios y DTOs.
- `security`: `JwtService`, `JwtAuthenticationFilter`, `AppUserDetailsService`, principal autenticado, `AccessControlService`, handlers JSON.
- `cart`: entidades, repositorios, DTOs, servicio y controller.
- `coupon`: entidades, repositorios, DTOs, servicio y controller.
- `loyalty`: entidades, repositorios, DTOs, servicio y controller.
- `review`: entidad, repositorio, DTOs, servicio y controller.
- `report`: queries, servicio y controller.
- `admin`: configuración básica de comisiones.
- `address`: repositorio, DTOs, servicio y controller para `/api/users/me/addresses`.
- `order`: `OrderService`, `OrderFactory`, controller, repositorios de items/historial y tracking DTO.
- `config/database/DevSeedRunner`: seed idempotente para perfil/flag dev.
- `common/exception/BusinessException`.
- Migración `V6__auth_indexes_and_backend_completion_support.sql`.

## 4. Código reemplazado o reforzado

- `SecurityConfig` fue reemplazado por configuración stateless con JWT y reglas por rol.
- `GlobalExceptionHandler` fue ampliado para errores de negocio, validación, rutas inexistentes y errores inesperados sin stack traces.
- Controllers de restaurantes, productos, categorías y promociones mantienen rutas existentes y agregan aliases `/api`.
- Servicios de restaurante/producto/categoría/promoción refuerzan permisos por dueño/admin.
- Delivery response ahora incluye dirección en texto plano, restaurante, resumen de items y estado del pedido.
- Pedido ahora persiste subtotal, descuento, impuestos, envío, propina, total, cupón, items e historial.

## 5. Endpoints disponibles

### Auth

| Método | Endpoint | Resultado esperado |
| --- | --- | --- |
| POST | `/api/auth/register` | `201`, registra cliente y devuelve JWT/refresh |
| POST | `/api/auth/login` | `200`, devuelve JWT/refresh |
| POST | `/api/auth/refresh` | `200`, rota token |
| POST | `/api/auth/logout` | `204`, revoca refresh token |
| GET | `/api/auth/me` | `200`, usuario autenticado |

### Usuarios y direcciones

| Método | Endpoint | Resultado esperado |
| --- | --- | --- |
| GET | `/api/users` | `200`, admin lista usuarios |
| POST | `/api/users` | `201`, admin crea usuario |
| GET | `/api/users/{id}` | `200` o `404` |
| PUT | `/api/users/{id}` | `200`, admin actualiza usuario |
| DELETE | `/api/users/{id}` | `204`, desactiva usuario |
| GET | `/api/users/me` | `200`, perfil propio |
| PUT | `/api/users/me` | `200`, actualiza perfil propio |
| GET | `/api/users/me/addresses` | `200`, direcciones propias |
| POST | `/api/users/me/addresses` | `201`, crea dirección |
| PUT | `/api/users/me/addresses/{id}` | `200`, actualiza dirección |
| DELETE | `/api/users/me/addresses/{id}` | `204`, elimina/desactiva dirección |

### Catálogo

| Método | Endpoint | Resultado esperado |
| --- | --- | --- |
| GET/POST | `/restaurants`, `/api/restaurants` | Público GET; escritura admin/restaurante |
| GET/PUT/PATCH | `/api/restaurants/{id}` | Consulta/actualización/desactivación |
| GET/PUT | `/api/restaurants/{id}/schedules` | Horarios |
| GET/POST | `/products`, `/api/products` | Público GET; escritura admin/restaurante |
| GET/PUT/PATCH | `/api/products/{id}` | Consulta/actualización/disponibilidad/desactivación |
| GET/POST | `/categories`, `/api/categories` | Público GET; escritura admin/restaurante |
| GET/PUT/PATCH | `/api/categories/{id}` | Consulta/actualización/desactivación |
| GET/POST | `/promotions`, `/api/promotions` | Público GET; escritura admin/restaurante |
| GET/PUT/PATCH | `/api/promotions/{id}` | Consulta/actualización/estado/desactivación |

### Carrito y pedidos

| Método | Endpoint | Resultado esperado |
| --- | --- | --- |
| GET | `/api/cart` | `200`, carrito activo del cliente |
| POST | `/api/cart/items` | `201`, agrega producto |
| PATCH | `/api/cart/items/{id}` | `200`, cambia cantidad |
| DELETE | `/api/cart/items/{id}` | `204`, elimina item |
| DELETE | `/api/cart` | `204`, vacía carrito |
| POST | `/api/orders` | `201`, crea pedido desde carrito |
| GET | `/api/orders/{id}` | `200`, detalle |
| GET | `/api/orders/my-history` | `200`, historial cliente |
| GET | `/api/orders/restaurant` | `200`, pedidos del restaurante propio |
| PATCH | `/api/orders/{id}/cancel` | `200`, cancela antes de confirmar |
| PATCH | `/api/orders/{id}/confirm` | `200`, restaurante confirma |
| PATCH | `/api/orders/{id}/reject` | `200`, restaurante rechaza |
| GET | `/api/orders/{id}/tracking` | `200`, tracking por polling REST |

### Delivery

| Método | Endpoint | Resultado esperado |
| --- | --- | --- |
| POST | `/api/deliveries/assign` | `201`, admin asigna repartidor disponible |
| GET | `/api/deliveries/my-orders` | `200`, repartidor ve asignaciones |
| PATCH | `/api/deliveries/{id}/status` | `200`, avanza `PICKED_UP`, `ON_THE_WAY`, `DELIVERED` |

### Reclamos, cupones, fidelidad, reviews, reportes y admin

| Método | Endpoint | Resultado esperado |
| --- | --- | --- |
| POST/GET | `/api/complaints` | Crear/listar reclamos |
| GET/PATCH | `/api/complaints/{id}` | Consultar/cambiar estado |
| POST/GET | `/api/coupons` | CRUD admin de cupones |
| GET/PUT/PATCH | `/api/coupons/{id}` | Consultar/actualizar/activar/desactivar |
| GET | `/api/loyalty` | Balance de puntos |
| POST | `/api/loyalty/redeem` | Canje básico |
| POST | `/api/reviews` | Calificar pedido entregado |
| GET | `/api/reviews/restaurant/{restaurantId}` | Reviews de restaurante |
| GET | `/api/reports/restaurants/most-ordered` | Reporte admin |
| GET | `/api/reports/admin-summary` | Resumen admin |
| POST/GET | `/api/admin/commissions` | Configuración/listado de comisiones |

## 6. Reglas de negocio implementadas

- Roles internos: `ADMIN`, `CUSTOMER`, `RESTAURANT`, `DELIVERY`.
- `CUSTOMER` equivale a `CLIENTE`; `DELIVERY` equivale a `REPARTIDOR`.
- JWT requerido para endpoints privados.
- Admin protege usuarios, cupones, reportes y comisiones.
- Restaurante solo administra recursos propios o admin.
- Carrito impide mezclar productos de restaurantes distintos.
- Pedido se crea desde carrito y toma snapshot de producto/precio.
- Pedido calcula subtotal, descuento, impuestos, envío, propina y total.
- Pedido solo puede cancelarse antes de confirmación.
- Delivery usa estados `ASSIGNED`, `PICKED_UP`, `ON_THE_WAY`, `DELIVERED`, `CANCELLED`.
- No se implementan WebSockets, rutas GPS ni navegación.
- Tracking se consulta por REST en `/api/orders/{id}/tracking`.
- Al entregar pedido se acumulan puntos de fidelidad.
- Calificaciones requieren pedido entregado y evitan duplicado.
- Seed dev es idempotente y actualiza credenciales conocidas.

## 7. Variables de entorno necesarias

No se deben commitear secretos reales. Variables esperadas:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `DB_POOL_MAX_SIZE`
- `DB_POOL_MIN_IDLE`
- `DB_CONNECTION_TIMEOUT_MS`
- `DB_IDLE_TIMEOUT_MS`
- `DB_MAX_LIFETIME_MS`
- `FLYWAY_BASELINE_ON_MIGRATE`
- `FLYWAY_BASELINE_VERSION`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_MINUTES`
- `JWT_REFRESH_TOKEN_DAYS`
- `DEV_SEED_ENABLED`
- `DEV_DELIVERY_USER_ID`

## 8. Datos seed dev

Activar con:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.seed.enabled=true"
```

Usuarios seed, todos con password `Password123!`:

- `admin.dev@example.com` - `ADMIN`
- `cliente.dev@example.com` - `CUSTOMER`
- `restaurante.dev@example.com` - `RESTAURANT`
- `repartidor.dev@example.com` - `DELIVERY`
- `repartidor.cercano.dev@example.com` - `DELIVERY`

También se crea/asegura:

- Dirección cliente: `Calle Dev 123`
- Restaurante: `Restaurante Dev`
- Categoría: `Combos Dev`
- Producto: `Combo Dev`
- Cupón: `DEV10`

## 9. Evidencia de validación

Comandos ejecutados:

```bash
./mvnw -q test
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --app.seed.enabled=true"
```

Resultado:

- Tests Maven: verdes.
- Spring Boot: arrancó correctamente en `8081`.
- Flyway: validó 7 migraciones y schema en versión 6.
- Seed dev: ejecutó sin error.

Pruebas HTTP manuales ejecutadas:

| Caso | Resultado |
| --- | --- |
| `GET /api/restaurants` | `200` |
| `GET /api/cart` sin token | `401` |
| `POST /api/auth/login` cliente seed | `200`, token y refresh presentes |
| `GET /api/auth/me` con JWT | `200` |
| `GET /api/cart` con JWT cliente | `200` |
| `POST /api/cart/items` | `201` |
| `POST /api/orders` con cupón `DEV10` | `201` |
| `PATCH /api/orders/{id}/confirm` con restaurante | `200` |
| `POST /api/deliveries/assign` con admin | `201` |
| `GET /api/deliveries/my-orders` con repartidor | `200` |
| `PATCH /api/deliveries/{id}/status` a `PICKED_UP` | `200` |
| `PATCH /api/deliveries/{id}/status` a `ON_THE_WAY` | `200` |
| `PATCH /api/deliveries/{id}/status` a `DELIVERED` | `200`; orden queda `DELIVERED` |

## 10. Pendientes reales

- Agregar más pruebas unitarias/MVC para todos los módulos nuevos; hoy se validó compilación, tests existentes y flujo HTTP manual.
- Separar DTO de actualización de usuario para no exigir password en `PUT /api/users/me` y `PUT /api/users/{id}`.
- Afinar reportes administrativos si el frontend necesita más filtros, fechas o paginación.
- Agregar paginación en listados grandes.
- Revisar si se quiere una política formal de refresh token reuse/revocation más estricta.
- Endurecer configuración de Swagger para producción si se despliega públicamente.

## 11. Riesgos de integración con frontend

- Algunos DTOs de respuesta crecieron, especialmente `OrderResponse` y `DeliveryResponse`; el frontend debe tolerar campos nuevos.
- Las rutas antiguas de catálogo se conservan, pero los módulos nuevos viven bajo `/api`.
- Los endpoints protegidos ahora requieren JWT real; cualquier frontend que dependía solo de `X-Dev-User-Id` debe migrar a login.
- `CUSTOMER`/`DELIVERY` son los nombres reales de roles en API, aunque en documentación del equipo equivalen a `CLIENTE`/`REPARTIDOR`.

## 12. Cómo ejecutar

Sin seed:

```bash
./mvnw spring-boot:run
```

Con seed dev:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.seed.enabled=true"
```

En puerto alterno:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --app.seed.enabled=true"
```

