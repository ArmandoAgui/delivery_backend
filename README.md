# Delivery Backend

Backend de una plataforma de delivery de comida construida con Spring Boot, PostgreSQL/PostGIS, Flyway, JWT, Docker y OpenAPI.

El proyecto soporta los flujos principales del negocio:

- clientes que buscan restaurantes, agregan productos al carrito, pagan y siguen pedidos;
- restaurantes que administran su catalogo, horarios, imagenes y pedidos;
- repartidores que reciben asignaciones automaticas, avanzan estados y reportan su ubicacion;
- administradores que gestionan usuarios, reclamos, cupones, comisiones y reportes.

## Estado Actual

El backend ya integra:

- arquitectura N-capas;
- autenticacion con JWT y refresh tokens persistentes;
- PostgreSQL con PostGIS para geolocalizacion;
- calculo de envio y ETA;
- carrito y pedido con recalculo seguro desde el backend;
- delivery automatico con asignacion por cercania;
- reclamos, reembolsos, fidelidad, monedero digital y calificaciones;
- carga de imagenes optimizadas para restaurantes y productos;
- documentacion Swagger/OpenAPI;
- despliegue con Docker Compose para desarrollo y EC2.

## Tecnologias

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- Bean Validation
- PostgreSQL 16 + PostGIS 3.4
- Flyway
- Springdoc OpenAPI
- HikariCP
- Maven Wrapper
- JUnit 5, Mockito, MockMvc

## Estructura General

La aplicacion sigue una arquitectura N-capas:

```text
controller -> service -> repository -> entity/database
```

Los contratos HTTP se manejan con DTOs y los errores se centralizan en un handler global.

### Capas principales

- `controller`: expone endpoints REST y valida entrada.
- `service`: contiene reglas de negocio y transacciones.
- `repository`: consultas JPA y SQL especializadas.
- `entity`: modelo persistente.
- `dto`: request/response del API.
- `mapper`: conversion entre entidades y DTOs.
- `security`: JWT, filtros y autorizacion por rol.
- `exception`: manejo uniforme de errores.

## Arquitectura De Despliegue

La version productiva corre en una sola instancia EC2 con Docker Compose:

- Nginx como entrada publica.
- Backend Spring Boot interno.
- Frontend React interno.
- PostgreSQL + PostGIS con volumen persistente.
- Certbot + Let’s Encrypt para HTTPS.

### Flujo de red

```text
Internet -> Nginx -> Frontend
Internet -> Nginx -> /api -> Backend
Internet -> Nginx -> /uploads -> Backend
Internet -> Nginx -> /swagger-ui y /v3/api-docs -> Backend
```

No se exponen directamente PostgreSQL ni el puerto interno del backend.

## Ejecucion Local

### 1. Requisitos

- Java 25
- Maven Wrapper
- Docker y Docker Compose
- PostgreSQL con PostGIS si no usas Docker

### 2. Variables necesarias

El proyecto lee variables desde `.env` o desde el entorno del sistema.

### 3. Compilar

```bash
./mvnw clean test
```

### 4. Ejecutar

```bash
./mvnw spring-boot:run
```

### 5. Swagger

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Docker

### Imagen del backend

El backend puede construirse como imagen independiente.

```bash
docker build -t delivery-backend:local .
```

### Ejecutar con Docker

Ejemplo con variables de entorno:

```bash
docker run --rm \
  --name delivery-backend \
  -e DB_URL=jdbc:postgresql://localhost:5432/delivery \
  -e DB_USER=delivery \
  -e DB_PASSWORD=delivery \
  -e JWT_SECRET=change-this-secret \
  -e DEV_SEED_ENABLED=false \
  delivery-backend:local
```

## Docker Compose

### Desarrollo local

El repositorio incluye un `docker-compose.yml` para levantar el stack completo:

- PostgreSQL 16 + PostGIS
- Backend Spring Boot
- Frontend React servido por Nginx

```bash
docker compose up --build
```

Puertos locales:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

### Produccion en EC2

Tambien existe `docker-compose.aws.yml`, pensado para una sola instancia EC2.

Levanta:

- `delivery-nginx`
- `delivery-frontend`
- `delivery-backend`
- `delivery-db`

Ejemplo:

```bash
cp .env.example .env
docker compose -f docker-compose.aws.yml --env-file .env up -d --build
```

## Variables De Entorno

Variables mas importantes:

| Variable | Descripcion |
| --- | --- |
| `DB_URL` | URL JDBC de PostgreSQL/PostGIS |
| `DB_USER` | Usuario de base de datos |
| `DB_PASSWORD` | Contrasena de base de datos |
| `JWT_SECRET` | Secreto para firmar tokens |
| `JWT_ACCESS_TOKEN_MINUTES` | Vida del access token |
| `JWT_REFRESH_TOKEN_DAYS` | Vida del refresh token |
| `CORS_ALLOWED_ORIGINS` | Origenes permitidos del frontend |
| `DEV_SEED_ENABLED` | Activa datos de desarrollo |
| `DEV_DELIVERY_USER_ID` | Usuario fallback para desarrollo |
| `DB_POOL_MAX_SIZE` | Tamano maximo del pool HikariCP |
| `DB_POOL_MIN_IDLE` | Minimo de conexiones ociosas |
| `DB_CONNECTION_TIMEOUT_MS` | Timeout de conexion |
| `DB_IDLE_TIMEOUT_MS` | Timeout idle |
| `DB_MAX_LIFETIME_MS` | Vida maxima de una conexion |
| `DB_KEEPALIVE_TIME_MS` | Keepalive para conexiones largas |
| `UPLOADS_ROOT_PATH` | Carpeta local de imagenes |
| `UPLOADS_PUBLIC_PATH` | Ruta publica de imagenes |
| `UPLOADS_MAX_FILE_SIZE_BYTES` | Tamano maximo de carga |
| `UPLOADS_RESTAURANT_MAX_WIDTH` | Ancho maximo de restaurante |
| `UPLOADS_PRODUCT_MAX_WIDTH` | Ancho maximo de producto |
| `UPLOADS_IMAGE_QUALITY` | Calidad WebP |

## Pool De Conexiones

La configuracion actual de HikariCP esta optimizada para una instancia EC2 con mas capacidad de concurrencia:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 20
      connection-timeout: 3000
      idle-timeout: 600000
      max-lifetime: 1800000
      keepalive-time: 300000
      validation-timeout: 5000
```

## Geolocalizacion

Se usa PostGIS con `GEOGRAPHY(Point,4326)` para:

- ubicar restaurantes y direcciones;
- calcular cercania;
- elegir repartidor mas cercano;
- estimar costo de envio y tiempo.

### Puntos clave

- las coordenadas se capturan con el frontend;
- el backend valida y persiste el `Point`;
- las consultas espaciales se hacen en PostgreSQL/PostGIS;
- el calculo no depende de la distancia “plana”, sino de consultas geoespaciales.

## Imagenes

Las imagenes de restaurantes y productos:

- se almacenan en disco;
- se convierten a WebP;
- se redimensionan antes de guardar;
- no se guardan como BLOB en la base de datos.

### Rutas habituales

```text
/uploads/restaurants/restaurant-<uuid>.webp
/uploads/products/product-<uuid>.webp
```

### Variables utiles

```text
UPLOADS_ROOT_PATH=/app/uploads
UPLOADS_PUBLIC_PATH=/uploads
UPLOADS_MAX_FILE_SIZE_BYTES=10485760
UPLOADS_RESTAURANT_MAX_WIDTH=1280
UPLOADS_PRODUCT_MAX_WIDTH=800
UPLOADS_IMAGE_QUALITY=0.82
```

## Reglas De Negocio Principales

### Clientes

- registro y login;
- carrito y checkout;
- uso de cupones;
- uso de monedero digital;
- reclamos y calificaciones;
- seguimiento de pedidos;
- descarga de factura.

### Restaurantes

- gestion de restaurante;
- categorias y productos;
- horarios;
- imagenes;
- confirmacion o rechazo de pedidos;
- dashboard de estadisticas.

### Repartidores

- reciben solicitudes automaticas;
- ven solo sus entregas activas;
- avanzan estados con una sola accion;
- actualizan ubicacion;
- reciben comision versionada por pedido.

### Administradores

- gestion de usuarios;
- activacion o desactivacion;
- reclamos y reembolsos;
- cupones;
- comisiones;
- reportes.

## Endpoints Clave

No es una lista exhaustiva, pero estos son los mas importantes:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`
- `POST /api/cart/items`
- `GET /api/cart`
- `POST /api/cart/quote`
- `POST /api/orders`
- `GET /api/orders/{id}/tracking`
- `PATCH /api/orders/{id}/confirm`
- `GET /api/deliveries/my-orders`
- `PATCH /api/deliveries/{id}/status`
- `GET /api/restaurants`
- `GET /api/restaurants/nearby`
- `GET /api/products/restaurant/{restaurantId}`
- `POST /api/complaints`
- `POST /api/reviews`
- `GET /api/admin/reportes`
- `GET /api/admin/users`
- `POST /api/admin/coupons`

Swagger muestra el contrato completo y siempre debe revisarse antes de integrar
frontend o pruebas de carga.

## Pruebas

### Ejecutar pruebas automatizadas

```bash
./mvnw clean test
```

### Probar el backend levantado

```bash
curl http://localhost:8080/swagger-ui/index.html
curl http://localhost:8080/v3/api-docs
```

### Prueba de carga

Se realizo una prueba con `k6` de `10,000 pedidos/minuto` en la instancia
EC2 mejorada, usando el flujo real de carrito + orden.

Resultado resumido:

- `10,018` pedidos exitosos;
- `100%` de solicitudes de orden exitosas;
- `p95` de creacion de orden por debajo de `1s`;
- el entorno quedo limpio despues de la corrida.

## Despliegue

### EC2

Guia recomendada:

- clonar backend y frontend en la misma instancia;
- configurar `.env`;
- levantar con `docker compose -f docker-compose.aws.yml --env-file .env up -d --build`;
- exponer solo `80` y `443` al publico;
- mantener PostgreSQL interno al host Docker.

### SSL / sslip.io

Se usa `sslip.io` para asociar la IP publica a un nombre de dominio utilizable
por Let's Encrypt sin comprar un dominio propio.

## Documentacion Relacionada

- [`PROJECT_TECHNICAL_EXPLANATION_GUIDE.md`](/home/armandoaguilar/Desktop/delevery_backend/PROJECT_TECHNICAL_EXPLANATION_GUIDE.md)
- [`DEPLOYMENT_SETUP_GUIDE.md`](/home/armandoaguilar/Desktop/delevery_backend/DEPLOYMENT_SETUP_GUIDE.md)
- [`DATABASE_ER_DIAGRAM.puml`](/home/armandoaguilar/Desktop/delevery_backend/DATABASE_ER_DIAGRAM.puml)

## Notas Finales

- La aplicacion esta pensada para evolucionar sin romper el contrato HTTP.
- Los totales y montos siempre se recalculan en backend.
- Las reglas de negocio dependen de estados, propiedad y validaciones de
  seguridad.
- Los datos de prueba, seeds y scripts de carga deben limpiarse despues de
  cada validacion.
