# Delivery Backend

Backend para una plataforma de delivery de comida donde restaurantes publican productos, clientes realizan pedidos y repartidores reciben asignaciones segun cercania. El proyecto esta construido con Spring Boot, PostgreSQL/PostGIS, Flyway y una API REST documentable con OpenAPI.

> Estado actual: el repositorio contiene la base del modelo de negocio, migraciones completas para la base de datos y el modulo de asignacion/seguimiento de repartidores. La autenticacion real todavia esta en modo desarrollo: Spring Security permite las peticiones y `AuthenticatedUserProvider` resuelve el usuario actual desde `X-Dev-User-Id` o desde `DEV_DELIVERY_USER_ID`.

## Objetivo del Proyecto

El sistema responde a la tematica **Servicio de Delivery de Comida**:

- restaurantes administran menus, precios y horarios;
- usuarios compran comida para entrega a domicilio;
- repartidores reciben pedidos y actualizan el estado de la entrega;
- administradores gestionan usuarios, reclamos y comisiones.

Tambien se dejo preparada la estructura de datos para cupones, fidelidad, facturacion, reclamos, reembolsos, agrupacion de pedidos y pagos simulados/Stripe.

## Tecnologias

- Java 25
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- Bean Validation
- PostgreSQL con PostGIS
- Flyway
- Springdoc OpenAPI
- Maven Wrapper
- JUnit 5, Mockito, MockMvc

## Docker

El backend puede construirse y ejecutarse como una imagen Docker independiente. La imagen no incluye base de datos; esta debe proveerse externamente, por ejemplo desde PostgreSQL/PostGIS local, un servicio administrado o un repositorio futuro de despliegue con Docker Compose, Nginx/Traefik y frontend.

## Docker Compose Local

El repositorio incluye un `docker-compose.yml` para levantar el stack completo en
desarrollo:

- PostgreSQL 16 con PostGIS.
- Backend Spring Boot.
- Frontend React servido por Nginx desde `../delevery_frontend`.

Levantar todo:

```bash
docker compose up --build
```

Requiere Docker Compose V2 (`docker compose`). Si tu instalacion solo tiene el
binario antiguo `docker-compose`, instala el plugin oficial de Compose antes de
usar este archivo.

Abrir:

```text
Frontend: http://localhost:5173
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
DB host:  localhost:5434
```

La base de datos queda persistida en el volumen Docker:

```text
delivery_postgres_data
```

Para apagar sin borrar datos:

```bash
docker compose down
```

Para borrar tambien la base local dockerizada:

```bash
docker compose down -v
```

Si necesitas cambiar puertos o credenciales:

```bash
cp compose.env.example compose.env
docker compose --env-file compose.env up --build
```

Por defecto el compose usa:

```text
POSTGRES_DB=delivery
POSTGRES_USER=delivery
POSTGRES_PASSWORD=delivery
POSTGRES_PORT=5434
```

> Nota: se usa `5434` en el host para no chocar con tu PostgreSQL local actual
> en `5433`. Dentro de Docker el backend se conecta a `db:5432`.

## Docker Compose En AWS EC2

Para una instancia EC2 de una sola maquina se incluye
`docker-compose.aws.yml`. Este archivo levanta:

- Nginx publico en el puerto `80`.
- Frontend React en red interna.
- Backend Spring Boot en red interna.
- PostgreSQL/PostGIS persistido en volumen Docker.

Preparar variables:

```bash
cp deploy/aws.env.example aws.env
```

Editar `aws.env` y cambiar al menos:

```text
POSTGRES_PASSWORD=<password-fuerte>
JWT_SECRET=<secreto-largo-y-privado>
CORS_ALLOWED_ORIGINS=http://<IP_PUBLICA_EC2>
```

Levantar el stack desde este repositorio, dejando el frontend como repositorio
hermano llamado `delivery_frontend`:

```bash
docker compose --env-file aws.env -f docker-compose.aws.yml up -d --build
```

Verificar:

```bash
docker compose --env-file aws.env -f docker-compose.aws.yml ps
docker compose --env-file aws.env -f docker-compose.aws.yml logs --tail=100
curl http://localhost/
curl http://localhost/api/auth/me
```

En AWS solo debe exponerse publicamente el puerto `80` y mantenerse `22` para
SSH. PostgreSQL y Spring Boot no se publican al host en este compose.

### Base De Datos En Render

Para Render, lo recomendado es usar una base PostgreSQL administrada compatible
con PostGIS y configurar el backend con variables de entorno:

```text
DB_URL=jdbc:postgresql://<host-render>:5432/<database>?sslmode=require
DB_USER=<usuario-render>
DB_PASSWORD=<password-render>
DEV_SEED_ENABLED=false
JWT_SECRET=<secreto-fuerte>
CORS_ALLOWED_ORIGINS=https://<frontend-render>.onrender.com
```

El contenedor de base de datos del `docker-compose.yml` es para desarrollo local.
En Render no conviene depender de un contenedor PostgreSQL efimero sin volumen
persistente administrado, porque podrias perder datos al redeploy.

### Construir Imagen

```bash
docker build -t delivery-backend:local .
```

### Ejecutar Contenedor

Ejemplo usando PostgreSQL/PostGIS local expuesto en `localhost:5433`:

```bash
docker run --rm \
  --name delivery-backend \
  --network host \
  -e SERVER_PORT=8080 \
  -e DB_URL=jdbc:postgresql://localhost:5433/postgres \
  -e DB_USER=armando \
  -e DB_PASSWORD=1234 \
  -e JWT_SECRET=delivery-development-secret-key-change-before-production-0123456789 \
  -e DEV_SEED_ENABLED=true \
  delivery-backend:local
```

Si se prefiere usar archivo de variables:

```bash
cp docker.env.example docker.env
docker run --rm --name delivery-backend --network host --env-file docker.env delivery-backend:local
```

> Nota: `docker.env` esta ignorado por Git para evitar subir secretos locales.

### Variables De Entorno

| Variable | Requerida | Descripcion | Ejemplo |
| --- | --- | --- | --- |
| `DB_URL` | Si | URL JDBC de PostgreSQL/PostGIS. | `jdbc:postgresql://db:5432/delivery` |
| `DB_USER` | Si | Usuario de base de datos. | `delivery_user` |
| `DB_PASSWORD` | Si | Password de base de datos. | `change-me` |
| `SERVER_PORT` | No | Puerto interno del backend. | `8080` |
| `SPRING_PROFILES_ACTIVE` | No | Perfil activo de Spring. | `prod` |
| `JWT_SECRET` | Si en produccion | Secreto para firmar JWT. Debe ser fuerte y privado. | `change-this-secret` |
| `JWT_ACCESS_TOKEN_MINUTES` | No | Minutos de vida del access token. | `60` |
| `JWT_REFRESH_TOKEN_DAYS` | No | Dias de vida del refresh token. | `14` |
| `CORS_ALLOWED_ORIGINS` | No | Origenes frontend permitidos separados por coma. | `https://app.example.com,http://localhost:5173` |
| `DEV_SEED_ENABLED` | No | Activa seed de desarrollo. Usar `false` en produccion. | `false` |
| `DEV_DELIVERY_USER_ID` | No | Usuario dev de fallback si aplica. | `018f...0003` |
| `FLYWAY_BASELINE_ON_MIGRATE` | No | Baseline para esquemas existentes. | `false` |
| `FLYWAY_BASELINE_VERSION` | No | Version de baseline Flyway. | `0` |
| `DB_POOL_MAX_SIZE` | No | Maximo de conexiones HikariCP. | `10` |
| `DB_POOL_MIN_IDLE` | No | Minimo de conexiones idle. | `2` |
| `DB_CONNECTION_TIMEOUT_MS` | No | Timeout de conexion. | `30000` |
| `DB_IDLE_TIMEOUT_MS` | No | Timeout idle. | `600000` |
| `DB_MAX_LIFETIME_MS` | No | Vida maxima de conexion. | `1800000` |
| `JAVA_OPTS` | No | Opciones JVM del contenedor. | `-XX:MaxRAMPercentage=75.0` |

### Puertos

- Backend HTTP: `8080` por defecto.
- Swagger UI: `/swagger-ui.html`.
- OpenAPI JSON: `/v3/api-docs`.

### Persistencia De Archivos

Actualmente el backend no escribe archivos de negocio en disco. La persistencia relevante vive en PostgreSQL/PostGIS. Si en el futuro se agregan cargas de imagenes, facturas generadas u otros archivos, esas rutas deberan externalizarse por variable de entorno y montarse como volumen en produccion.

### Preparacion Para Deploy

La imagen queda lista para integrarse despues en un repositorio `deploy` con:

- PostgreSQL/PostGIS como servicio externo.
- Frontend como servicio independiente.
- Nginx o Traefik como reverse proxy.
- Certificados TLS gestionados fuera del backend.
- Variables sensibles inyectadas mediante secretos del entorno.

## Arquitectura

El proyecto sigue una arquitectura por modulos de dominio:

```text
src/main/java/sv/edu/uca/delivery/backend
├── address       # direcciones de usuarios
├── auth          # roles
├── common        # manejo global de errores
├── delivery      # asignacion y ciclo de vida de entregas
├── order         # pedidos
├── restaurant    # restaurantes
├── security      # usuario autenticado/configuracion de seguridad
├── user          # usuarios
└── util          # generador UUID v7
```

Capas usadas en el modulo `delivery`:

- `controller`: expone endpoints REST.
- `dto`: define contratos de entrada/salida.
- `service`: concentra reglas de negocio y transacciones.
- `repository`: consultas JPA y geoespaciales.
- `entity`: mapeo de tablas.
- `exception`: errores de negocio con codigos HTTP.
- `mapper`: conversion de entidades a respuestas.

## Reglas de Negocio Implementadas

Actualmente el backend implementa el nucleo de delivery:

- asignacion automatica de un repartidor activo con rol `DELIVERY`;
- busqueda del repartidor disponible mas cercano al restaurante usando PostGIS;
- bloqueo pesimista de pedidos/asignaciones para evitar carreras en alta concurrencia;
- validacion de pedidos cancelados o entregados antes de asignar;
- prevencion de doble asignacion por pedido;
- prevencion de asignar repartidores con entregas activas;
- consulta de pedidos asignados al repartidor actual;
- transiciones controladas de estado:
  - `ASSIGNED -> PICKED_UP`
  - `PICKED_UP -> ON_THE_WAY`
  - `ON_THE_WAY -> DELIVERED`
- actualizacion del estado del pedido cuando la entrega pasa a `ON_THE_WAY` o `DELIVERED`;
- manejo uniforme de errores de validacion, negocio y recursos no encontrados.

## Funcionalidades Modeladas en Base de Datos

Las migraciones incluyen tablas e indices para cubrir la mayoria de la especificacion general:

- roles y usuarios;
- direcciones con ubicacion geografica;
- restaurantes con ubicacion, estado y horarios;
- categorias y productos de menu;
- carritos y items de carrito;
- pedidos, items y desglose monetario;
- pagos simulados o Stripe;
- asignaciones de delivery;
- historial de estados del pedido;
- tracking de ubicacion de repartidores;
- pedidos agrupados por repartidor;
- reviews/calificaciones;
- reclamos y reembolsos;
- facturas;
- cupones, redenciones y fidelidad;
- comisiones por restaurante.

## Roles

| Rol | Responsabilidad |
| --- | --- |
| `ADMIN` | Gestiona usuarios, reclamos y configuracion de comisiones. |
| `CUSTOMER` | Busca restaurantes, crea pedidos, paga, califica y consulta historial. |
| `RESTAURANT` | Administra restaurante, horarios, categorias, productos y pedidos recibidos. |
| `DELIVERY` | Recibe asignaciones, actualiza estado y reporta ubicacion. |

## Endpoints Actuales

Base path: `/api/deliveries`

| Metodo | Endpoint | Descripcion | Respuesta |
| --- | --- | --- | --- |
| `POST` | `/assign` | Asigna automaticamente un repartidor cercano a un pedido. | `201 Created` |
| `GET` | `/my-orders` | Lista las asignaciones del repartidor actual. | `200 OK` |
| `PATCH` | `/{id}/status` | Actualiza el estado de una asignacion. | `200 OK` |

### `POST /api/deliveries/assign`

Request:

```json
{
  "orderId": "018f0000-0000-7000-8000-000000000401"
}
```

Response:

```json
{
  "id": "018f0000-0000-7000-8000-000000000601",
  "orderId": "018f0000-0000-7000-8000-000000000401",
  "deliveryUserId": "018f0000-0000-7000-8000-000000000003",
  "deliveryUserName": "Repartidor Cercano",
  "status": "ASSIGNED",
  "orderStatus": "READY_FOR_PICKUP",
  "assignedAt": "2026-05-08T18:00:00",
  "pickedUpAt": null,
  "deliveredAt": null,
  "createdAt": "2026-05-08T18:00:00"
}
```

### `PATCH /api/deliveries/{id}/status`

Request:

```json
{
  "status": "PICKED_UP"
}
```

Estados validos del delivery:

- `ASSIGNED`
- `PICKED_UP`
- `ON_THE_WAY`
- `DELIVERED`
- `CANCELLED`

Nota: `CANCELLED` existe en el modelo, pero este endpoint no permite cancelar asignaciones.

## Codigos HTTP y Errores

El backend usa `GlobalExceptionHandler` para entregar errores consistentes:

| Caso | Codigo |
| --- | --- |
| Creacion de asignacion exitosa | `201 Created` |
| Consulta/actualizacion exitosa | `200 OK` |
| Body invalido o campos requeridos ausentes | `400 Bad Request` |
| Pedido/asignacion no encontrada | `404 Not Found` |
| Regla de negocio incumplida | `409 Conflict` |

Formato de error:

```json
{
  "timestamp": "2026-05-08T18:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Order already has a delivery assignment",
  "path": "/api/deliveries/assign",
  "details": []
}
```

## Base de Datos

La base esta versionada con Flyway en:

```text
src/main/resources/db/migration
```

Migraciones principales:

- `V1__init_schema.sql`: esquema inicial de delivery, productos, pedidos, pagos, reviews, reclamos y cupones.
- `V2__extend_delivery_schema.sql`: carritos, horarios, tracking, agrupacion de pedidos, reembolsos, facturas, fidelidad y comisiones.
- `V3__convert_transactional_ids_to_uuid_v7.sql`: conversion de tablas transaccionales a UUID generados por backend.
- `V4__align_delivery_assignment_statuses.sql`: alinea estados de delivery con el modulo Java.

Datos de prueba manuales:

```text
src/main/resources/db/seed/delivery_test_data.sql
```

## Diagrama Entidad-Relacion

```mermaid
erDiagram
    ROLES ||--o{ USERS : assigns
    USERS ||--o{ ADDRESSES : owns
    USERS ||--o| RESTAURANTS : manages
    RESTAURANTS ||--o{ RESTAURANT_SCHEDULES : opens
    RESTAURANTS ||--o{ CATEGORIES : groups
    RESTAURANTS ||--o{ PRODUCTS : offers
    CATEGORIES ||--o{ PRODUCTS : contains
    USERS ||--o{ CARTS : creates
    CARTS ||--o{ CART_ITEMS : contains
    PRODUCTS ||--o{ CART_ITEMS : selected
    USERS ||--o{ ORDERS : places
    RESTAURANTS ||--o{ ORDERS : receives
    ADDRESSES ||--o{ ORDERS : destination
    ORDERS ||--o{ ORDER_ITEMS : contains
    PRODUCTS ||--o{ ORDER_ITEMS : snapshot
    ORDERS ||--o| DELIVERY_ASSIGNMENTS : assigned
    USERS ||--o{ DELIVERY_ASSIGNMENTS : delivers
    USERS ||--o{ DELIVERY_LOCATIONS : reports
    ORDERS ||--o{ ORDER_STATUS_HISTORY : tracks
    USERS ||--o{ DELIVERY_BATCHES : handles
    DELIVERY_BATCHES ||--o{ DELIVERY_BATCH_ORDERS : groups
    ORDERS ||--o{ DELIVERY_BATCH_ORDERS : included
    ORDERS ||--o{ PAYMENTS : pays
    ORDERS ||--o{ REVIEWS : receives
    ORDERS ||--o{ COMPLAINTS : reports
    PAYMENTS ||--o{ REFUNDS : reverses
    COMPLAINTS ||--o{ REFUNDS : justifies
    ORDERS ||--o| INVOICES : bills
    COUPONS ||--o{ COUPON_REDEMPTIONS : redeems
    USERS ||--o{ LOYALTY_ACCOUNTS : has
    LOYALTY_ACCOUNTS ||--o{ LOYALTY_TRANSACTIONS : records
    RESTAURANTS ||--o{ RESTAURANT_COMMISSIONS : configures
```

## Escalabilidad

El desafio plantea que la creacion de pedidos puede recibir hasta 10.000 pedidos por minuto y que el objeto pedido contiene partes opcionales: items, descuentos, propina, envio, impuestos y pagos.

Decisiones ya presentes:

- UUID v7 generados en backend para IDs transaccionales, utiles para orden temporal y menor friccion en inserciones distribuidas.
- Bloqueos pesimistas y `SKIP LOCKED` en asignacion de repartidores para reducir colisiones cuando varios pedidos se asignan al mismo tiempo.
- PostGIS e indices geoespaciales para busqueda por distancia.
- HikariCP configurable por variables de entorno.
- Separacion por capas para mantener reglas de negocio transaccionales en servicios.

Pendiente recomendado para completar este punto:

- implementar un `OrderBuilder` o fabrica de pedidos para construir ordenes complejas sin constructores largos ni setters dispersos;
- encapsular calculos de subtotal, envio, descuentos, impuestos y propina en servicios de dominio;
- agregar eventos/asynchrony para tracking y notificaciones de hora pico;
- agregar pruebas de concurrencia y carga sobre creacion/asignacion de pedidos.

## Configuracion Local

1. Crear un archivo `.env` basado en `.env.example`.
2. Completar `DB_PASSWORD`.
3. Verificar que la base PostgreSQL tenga PostGIS habilitado.
4. Ejecutar la aplicacion.

Variables principales:

```properties
DB_URL=jdbc:postgresql://host:5432/postgres?sslmode=require
DB_USER=postgres
DB_PASSWORD=
DB_POOL_MAX_SIZE=10
DB_POOL_MIN_IDLE=2
DEV_DELIVERY_USER_ID=018f0000-0000-7000-8000-000000000003
```

## Ejecucion

```bash
./mvnw spring-boot:run
```

La aplicacion queda disponible por defecto en:

```text
http://localhost:8080
```

Documentacion OpenAPI:

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

## Pruebas

```bash
./mvnw test
```

Resultado documentado:

```text
docs/testing/delivery-endpoints-test-results.md
```

La suite cubre:

- contrato HTTP del controller de delivery con MockMvc;
- validaciones de request;
- reglas de negocio del service;
- generador UUID v7;
- smoke test de la aplicacion.

## Pruebas Manuales con Datos Seed

Despues de levantar la aplicacion y cargar `delivery_test_data.sql`, se puede probar:

```bash
curl -X POST http://localhost:8080/api/deliveries/assign \
  -H "Content-Type: application/json" \
  -d '{"orderId":"018f0000-0000-7000-8000-000000000401"}'
```

Para simular otro repartidor en desarrollo:

```bash
curl http://localhost:8080/api/deliveries/my-orders \
  -H "X-Dev-User-Id: 018f0000-0000-7000-8000-000000000003"
```

## Despliegue

La configuracion esta preparada para usar variables de entorno en nube:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- variables opcionales de pool HikariCP
- `FLYWAY_BASELINE_ON_MIGRATE` si se conecta contra una base existente

Pendiente de entrega:

- URL publica del backend desplegado.
- URL del frontend desplegado.
- Video guia del despliegue.
- Reporte de aportes individuales.

## Pendientes del Proyecto

- Implementar autenticacion real con JWT o sesiones y reglas por rol.
- Completar endpoints de restaurantes, productos, carritos, pedidos, pagos, reclamos, reviews y administracion.
- Integrar Stripe o mantener un flujo de pago simulado.
- Agregar notificaciones para hora pico y seguimiento en tiempo real.
- Agregar endpoints de reportes, incluyendo restaurantes mas pedidos.
- Publicar documentacion final de API y despliegue.
