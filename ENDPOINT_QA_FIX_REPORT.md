# Endpoint QA Fix Report

Fecha: 2026-06-17

## Problema Revisado

El cliente no podia completar un pedido aunque ya tenia productos en el carrito.

Durante la reproduccion se encontro que habia dos problemas combinados:

- El frontend no resolvia bien el caso de cliente sin direccion: el checkout quedaba bloqueado sin una forma inmediata de crear direccion.
- El backend devolvia `500 Internal Server Error` al registrar clientes con telefono duplicado, porque `users.phone` es unico en base de datos y no habia validacion previa.

## Cambios Aplicados

### Backend

- Se agrego validacion previa de telefono duplicado en registro.
- Se agrego validacion previa de email/telefono duplicado en creacion y actualizacion de usuarios.
- Los conflictos de email o telefono ahora responden `409 Conflict` en lugar de `500`.
- Se agrego busqueda publica de restaurantes:
  - `GET /api/restaurants/search?q=...`
- Se agrego busqueda publica de productos:
  - `GET /api/products/search?q=...`
- Se dejo publico `GET /api/restaurants/{id}` para que cliente pueda ver detalle del restaurante.
- Se ajusto autorizacion de delivery ajeno para responder `403 Forbidden`.

### Frontend

- Checkout ya no manda cupon por defecto.
- Checkout permite crear direccion si el cliente no tiene ninguna.
- La creacion de pedido envia solo campos validos:
  - `deliveryAddressId`
  - `tipAmount`
  - `couponCode` solo si fue escrito
  - `notes` solo si fue escrito
- Catalogo de restaurantes tiene busqueda.
- Detalle de restaurante consulta datos reales del restaurante.
- Detalle de restaurante permite busqueda de productos.

## Flujo E2E Probado

### Cliente

| Paso | Endpoint | Resultado |
| --- | --- | --- |
| Registro cliente nuevo | `POST /api/auth/register` | `201 Created` |
| Perfil autenticado | `GET /api/auth/me` | `200 OK` |
| Listar restaurantes | `GET /api/restaurants` | `200 OK` |
| Buscar restaurantes | `GET /api/restaurants/search?q=dev` | `200 OK` |
| Restaurantes cercanos PostGIS | `GET /api/restaurants/nearby?lat=13.6929&lng=-89.2182&radiusKm=50` | `200 OK` |
| Detalle restaurante | `GET /api/restaurants/{id}` | `200 OK` |
| Productos por restaurante | `GET /api/products/restaurant/{restaurantId}` | `200 OK` |
| Buscar productos | `GET /api/products/search?q=combo` | `200 OK` |
| Vaciar carrito | `DELETE /api/cart` | `204 No Content` |
| Agregar item al carrito | `POST /api/cart/items` | `201 Created` |
| Consultar carrito | `GET /api/cart` | `200 OK` |
| Crear pedido sin direccion | `POST /api/orders` | `400 Bad Request` esperado |
| Crear direccion | `POST /api/users/me/addresses` | `201 Created` |
| Crear pedido desde carrito | `POST /api/orders` | `201 Created` |
| Tracking inicial | `GET /api/orders/{id}/tracking` | `200 OK` |

### Restaurante

| Paso | Endpoint | Resultado |
| --- | --- | --- |
| Login restaurante | `POST /api/auth/login` | `200 OK` |
| Pedidos del restaurante | `GET /api/orders/restaurant` | `200 OK` |
| Confirmar pedido | `PATCH /api/orders/{id}/confirm` | `200 OK` |

Al confirmar, el backend asigno automaticamente un repartidor y el tracking mostro `deliveryStatus=ASSIGNED`.

### Repartidor

| Paso | Endpoint | Resultado |
| --- | --- | --- |
| Login repartidor | `POST /api/auth/login` | `200 OK` |
| Entregas asignadas | `GET /api/deliveries/my-orders` | `200 OK` |
| Cambiar a recogido | `PATCH /api/deliveries/{id}/status` con `PICKED_UP` | `200 OK` |
| Cambiar a en camino | `PATCH /api/deliveries/{id}/status` con `ON_THE_WAY` | `200 OK` |
| Cambiar a entregado | `PATCH /api/deliveries/{id}/status` con `DELIVERED` | `200 OK` |

El tracking final del cliente mostro `status=DELIVERED` y `deliveryStatus=DELIVERED`.

### Admin

| Endpoint | Resultado |
| --- | --- |
| `GET /api/users` | `200 OK` |
| `GET /api/reports/admin-summary` | `200 OK` |
| `GET /api/reports/restaurants/most-ordered` | `200 OK` |
| `GET /api/coupons` | `200 OK` |
| `GET /api/complaints` | `200 OK` |
| `GET /api/admin/commissions` | `200 OK` |

## Endpoints Disponibles En Swagger

Swagger expone correctamente los grupos principales:

- Auth
- Users
- Addresses
- Restaurants
- Products
- Categories
- Promotions
- Cart
- Orders
- Deliveries
- Complaints
- Coupons
- Loyalty
- Reviews
- Reports
- Admin

Endpoint docs:

- `GET /v3/api-docs`
- `GET /swagger-ui/index.html`

## Validacion Tecnica

| Comando | Resultado |
| --- | --- |
| `./mvnw test -q` | Exitoso |
| `npm run lint` | Exitoso con warnings de hooks existentes |
| `npm run build` | Exitoso |

## Pendientes Reales

- Limpiar warnings de `react-hooks/exhaustive-deps` en `src/App.tsx`.
- Agregar pruebas automatizadas E2E completas con Playwright o similar.
- Si el equipo quiere telefonos reutilizables en ambiente demo, cambiar la restriccion unica de `users.phone`; por ahora se conserva porque la base ya la define asi.
- Revisar si `GET /actuator/health` debe ser publico. Actualmente responde `401`, lo cual es seguro pero puede requerir ajuste para Docker/monitoreo.

