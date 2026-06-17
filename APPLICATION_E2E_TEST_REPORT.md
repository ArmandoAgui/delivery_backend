# Application E2E Test Report

Fecha de prueba: 2026-06-17  
Frontend probado: `http://localhost:5173`  
API probada principalmente via proxy frontend: `http://localhost:5173/api`

## 1. Resumen Ejecutivo

El sistema esta funcional para una demostracion academica del flujo principal de Delivery. Se valido login por roles, registro de cliente, carrito, creacion/cancelacion de pedidos, confirmacion por restaurante, asignacion automatica de repartidor, tracking REST, entrega, reclamo, review, fidelidad, reportes, cupones y seguridad basica.

Principales hallazgos:

- El flujo completo de negocio funciona de punta a punta sin asignacion manual del administrador.
- PostGIS funciona para busqueda cercana y el backend devuelve estimaciones de envio/ETA/distancia.
- El frontend sirve las rutas principales por rol y consume la API por proxy sin CORS.
- Hay funcionalidades parciales de UI: algunas paginas existen pero muestran datos crudos o vistas minimas.
- El cliente recibe `403` al consultar detalle de restaurante por ID, aunque puede listar restaurantes y ver productos.
- La proteccion contra modificar entregas ajenas funciona, pero devuelve `409` en vez de un `403` semanticamente mas correcto.

## 2. Checklist De Funcionalidades

| Funcionalidad | Estado | Observacion |
| --- | --- | --- |
| Frontend `/login` disponible | ✅ Funciona | `GET /login` respondio `200`. |
| Registro cliente | ✅ Funciona | `POST /api/auth/register` respondio `201` y devolvio token. |
| Login admin | ✅ Funciona | `admin.dev@example.com` respondio `200`. |
| Login cliente | ✅ Funciona | `cliente.dev@example.com` respondio `200`. |
| Login restaurante | ✅ Funciona | `restaurante.dev@example.com` respondio `200`. |
| Login repartidor | ✅ Funciona | `repartidor.dev@example.com` respondio `200`. |
| Cierre de sesion | ⚠️ Parcial | Cliente HTTP tiene logout; no se ejecuto por script porque invalida el token de sesion de prueba. |
| Persistencia de sesion | ⚠️ Parcial | Implementada en `localStorage`; no validada con navegador automatizado. |
| Acceso sin autenticacion | ✅ Funciona | `GET /api/cart` sin token devolvio `401`. |
| Token invalido | ✅ Funciona | `GET /api/auth/me` con token invalido devolvio `401`. |
| Restriccion por rol | ✅ Funciona | Cliente consultando `/api/users` devolvio `403`. |
| Rutas frontend por rol | ✅ Funciona | Rutas principales respondieron `200` por Vite SPA. |
| Perfil cliente | ✅ Funciona | `GET /api/auth/me` y `/api/users/me` disponibles. |
| Direcciones cliente | ✅ Funciona | `GET /api/users/me/addresses` devolvio direccion seed. |
| Gestion completa de direcciones en UI | ⚠️ Parcial | El endpoint existe; la pagina actual muestra datos, no CRUD completo visual. |
| Listado de restaurantes | ✅ Funciona | `GET /api/restaurants` devolvio restaurantes activos. |
| Detalle de restaurante por cliente | ⚠️ Parcial | `GET /api/restaurants/{id}` con cliente devolvio `403`; la UI evita esto usando productos por restaurante. |
| Busqueda por nombre | 🚫 No implementada | No se encontro endpoint/UI especifico de busqueda textual. |
| Busqueda por categoria | ✅ Funciona | `GET /api/products/category/{id}` respondio `200`. |
| Busqueda por cercania | ✅ Funciona | `GET /api/restaurants/nearby?lat=&lng=&radiusKm=` respondio `200`. |
| Promociones por restaurante | ⚠️ Parcial | Endpoint respondio `200`, pero la data seed devolvio lista vacia. |
| Menu/productos por restaurante | ✅ Funciona | `GET /api/products/restaurant/{id}` respondio `200`. |
| Agregar productos al carrito | ✅ Funciona | `POST /api/cart/items` respondio `201`. |
| Modificar cantidades | ✅ Funciona | `PATCH /api/cart/items/{id}` respondio `200`. |
| Eliminar producto del carrito | ✅ Funciona | `DELETE /api/cart/items/{id}` respondio `204`. |
| Vaciar carrito | ✅ Funciona | `DELETE /api/cart` respondio `204`. |
| Subtotal carrito | ✅ Funciona | `GET /api/cart` devolvio subtotal e items. |
| Costo de envio estimado | ✅ Funciona | `GET /api/cart` devolvio `estimatedDeliveryFee`. |
| Tiempo estimado | ✅ Funciona | `GET /api/cart` y tracking devuelven ETA. |
| Distancia estimada | ✅ Funciona | `GET /api/cart` y tracking devuelven `distanceKm`. |
| Horario pico | ✅ Funciona | Campo `peakDemand` disponible; en esta prueba dependio de la hora local. |
| Aplicar cupon | ✅ Funciona | Pedido creado con `DEV10`; `usedCount` aumento. |
| Propina | ✅ Funciona | Pedido creado con `tipAmount`. |
| Crear pedido | ✅ Funciona | `POST /api/orders` respondio `201`. |
| Cancelar antes de confirmacion | ✅ Funciona | `PATCH /api/orders/{id}/cancel` respondio `200` y estado `CANCELLED`. |
| Historial de pedidos | ✅ Funciona | `GET /api/orders/my-history` usado indirectamente por flujo y disponible. |
| Detalle de pedido | ✅ Funciona | Tracking y pedidos devuelven detalle operativo; endpoint `/api/orders/{id}` existe. |
| Tracking REST | ✅ Funciona | `GET /api/orders/{id}/tracking` respondio `200` antes y despues de entrega. |
| Reclamo despues de entrega | ✅ Funciona | `POST /api/complaints` respondio `201`. |
| Fidelidad | ✅ Funciona | `GET /api/loyalty` mostro puntos acumulados despues de entrega. |
| Calificar restaurante/repartidor | ✅ Funciona | `POST /api/reviews` respondio `201` para pedido entregado. |
| Dashboard restaurante | ✅ Funciona | Ruta frontend existe y pedidos/productos cargan por API. |
| Edicion restaurante | ⚠️ Parcial | Backend tiene `PUT`; UI actual no tiene formulario completo de edicion. |
| Gestion de horarios | ⚠️ Parcial | `GET /api/restaurants/{id}/schedules` respondio `200`, pero devolvio lista vacia y UI es minima. |
| CRUD productos restaurante | ✅ Funciona | `POST /api/products` como restaurante respondio `201`. |
| Visualizacion pedidos restaurante | ✅ Funciona | `GET /api/orders/restaurant` respondio `200`. |
| Aceptar pedido | ✅ Funciona | `PATCH /api/orders/{id}/confirm` respondio `200`. |
| Rechazar pedido | ⚠️ Parcial | Endpoint existe y UI tiene accion; no se ejecuto para no alterar el pedido de flujo principal. |
| Asignacion automatica repartidor | ✅ Funciona | Confirmar pedido creo asignacion visible en `/api/deliveries/my-orders`. |
| Admin asignacion manual cotidiana | ✅ Funciona | `POST /api/deliveries/assign` devolvio `403`; no esta visible en UI. |
| Dashboard repartidor | ✅ Funciona | Ruta existe y `GET /api/deliveries/my-orders` devolvio asignacion. |
| Direccion de entrega | ✅ Funciona | Delivery response incluyo direccion en texto plano. |
| Estado `ASSIGNED` | ✅ Funciona | Asignacion inicial devolvio `ASSIGNED`. |
| Estado `PICKED_UP` | ✅ Funciona | `PATCH /api/deliveries/{id}/status` respondio `200`. |
| Estado `ON_THE_WAY` | ✅ Funciona | `PATCH /api/deliveries/{id}/status` respondio `200`. |
| Estado `DELIVERED` | ✅ Funciona | `PATCH /api/deliveries/{id}/status` respondio `200`. |
| Repartidor modifica entrega ajena | ✅ Funciona | Bloqueado; devolvio `409` con mensaje de pertenencia. |
| Historial repartidor | ⚠️ Parcial | Ruta UI existe; reutiliza listado de entregas, no separa historial cerrado. |
| Admin usuarios | ✅ Funciona | `GET /api/users` respondio `200`. |
| Admin reclamos | ✅ Funciona | `GET /api/complaints` respondio `200`. |
| Admin cupones | ✅ Funciona | `GET /api/coupons` respondio `200`. |
| Admin reportes | ✅ Funciona | Reportes devolvieron ordenes/revenue. |
| Admin comisiones | ✅ Funciona | `GET /api/admin/commissions` respondio `200`, lista vacia. |
| Admin gestion restaurantes | 🚫 No implementada en UI | No hay pagina admin dedicada para restaurantes. |
| Pedidos agrupados repartidor | 🚫 No implementada visible | Se permite consultar entregas, pero no hay UI/flujo de agrupacion. |
| Facturacion/resumen | ⚠️ Parcial | Pedido devuelve desglose monetario; no hay factura formal visual. |
| CORS/proxy frontend | ✅ Funciona | Login via `http://localhost:5173/api/auth/login` respondio `200`. |

## 3. Problemas Encontrados

### P1. Cliente no puede consultar detalle de restaurante por ID

- Error observado: `GET /api/restaurants/{id}` con rol cliente devolvio `403`.
- Pasos para reproducir:
  1. Login como `cliente.dev@example.com`.
  2. Ejecutar `GET /api/restaurants`.
  3. Tomar un `id`.
  4. Ejecutar `GET /api/restaurants/{id}`.
- Posible causa: `RestaurantService.findById` llama `requireOwner(restaurant)`, aunque el detalle publico deberia ser visible para clientes.
- Severidad: Media.
- Componente afectado: Backend/API/Seguridad.

### P2. Entrega ajena bloqueada con codigo HTTP semantico incorrecto

- Error observado: otro repartidor no puede modificar la entrega, pero recibe `409 Conflict`.
- Pasos para reproducir:
  1. Crear pedido y confirmar para asignar a `repartidor.dev@example.com`.
  2. Login como `repartidor.cercano.dev@example.com`.
  3. Intentar `PATCH /api/deliveries/{assignmentId}/status`.
- Posible causa: excepcion de negocio de delivery mapea a `409` para todos los casos.
- Severidad: Baja/Media.
- Componente afectado: Backend/API/Seguridad.
- Esperado recomendado: `403 Forbidden`.

### P3. Varias paginas frontend son funcionales pero minimas

- Error observado: paginas como perfil restaurante, horarios, historial repartidor y comisiones muestran dashboards o JSON crudo, no formularios finales.
- Pasos para reproducir:
  1. Navegar a `/restaurante/horarios`, `/repartidor/historial`, `/admin/comisiones`.
- Posible causa: MVP priorizo rutas reales e integracion sobre UI CRUD exhaustiva.
- Severidad: Media para presentacion.
- Componente afectado: Frontend/UX.

### P4. Busqueda por nombre no esta disponible

- Error observado: no hay endpoint ni UI especifica para buscar restaurantes/productos por texto.
- Pasos para reproducir:
  1. Revisar rutas y endpoints usados en E2E.
  2. No existe busqueda textual dedicada.
- Posible causa: se implementaron listados, categoria y cercania, pero no filtro por nombre.
- Severidad: Media.
- Componente afectado: Backend/Frontend.

### P5. Promociones sin datos visibles

- Error observado: `GET /api/promotions/restaurant/{id}` devolvio `200` con `[]`.
- Pasos para reproducir:
  1. Login.
  2. Consultar promociones del restaurante seed.
- Posible causa: seed no incluye promocion activa o no hay promocion configurada.
- Severidad: Baja.
- Componente afectado: Base de datos/Datos seed/UX.

## 4. Integracion

| Area | Resultado |
| --- | --- |
| Frontend -> Backend por proxy | Funciona. Login, pedidos y modulos operan via `localhost:5173/api`. |
| CORS | Sin problema observado porque se uso proxy de Vite. |
| DTO carrito | Compatible; frontend recibe estimaciones nuevas. |
| DTO pedidos | Compatible; respuestas incluyen desglose monetario y ETA. |
| DTO tracking | Compatible; incluye delivery, repartidor, ETA y direccion. |
| Seguridad API | Funciona en auth, roles y rutas protegidas. Hay mejora semantica pendiente en entrega ajena. |
| Base de datos | Persistio pedidos, cupones, reclamos, reviews, fidelidad y productos creados por prueba. |
| PostGIS | Funciona en busqueda cercana y estimacion de distancia. |

## 5. Evidencia De Prueba

Credenciales usadas:

- `admin.dev@example.com`
- `cliente.dev@example.com`
- `restaurante.dev@example.com`
- `repartidor.dev@example.com`
- `repartidor.cercano.dev@example.com`
- Password: `Password123!`

Endpoints clave validados:

- `POST /api/auth/register`: `201`
- `POST /api/auth/login`: `200`
- `GET /api/restaurants`: `200`
- `GET /api/restaurants/nearby`: `200`
- `GET /api/products/restaurant/{id}`: `200`
- `GET /api/products/category/{id}`: `200`
- `GET /api/users/me/addresses`: `200`
- `POST /api/cart/items`: `201`
- `PATCH /api/cart/items/{id}`: `200`
- `DELETE /api/cart/items/{id}`: `204`
- `DELETE /api/cart`: `204`
- `POST /api/orders`: `201`
- `PATCH /api/orders/{id}/cancel`: `200`
- `PATCH /api/orders/{id}/confirm`: `200`
- `POST /api/deliveries/assign`: `403`
- `GET /api/deliveries/my-orders`: `200`
- `PATCH /api/deliveries/{id}/status`: `200`
- `GET /api/orders/{id}/tracking`: `200`
- `POST /api/complaints`: `201`
- `POST /api/reviews`: `201`
- `GET /api/loyalty`: `200`
- `GET /api/reports/restaurants/most-ordered`: `200`
- `GET /api/reports/admin-summary`: `200`

Rutas frontend validadas con `200`:

- `/login`, `/register`, `/cliente`, `/cliente/restaurantes`, `/cliente/carrito`, `/cliente/checkout`, `/cliente/pedidos`, `/cliente/direcciones`, `/cliente/perfil`, `/cliente/fidelidad`, `/cliente/reclamos`, `/cliente/calificaciones`.
- `/restaurante`, `/restaurante/perfil`, `/restaurante/menu`, `/restaurante/productos`, `/restaurante/horarios`, `/restaurante/pedidos`.
- `/repartidor`, `/repartidor/entregas`, `/repartidor/historial`.
- `/admin`, `/admin/usuarios`, `/admin/reclamos`, `/admin/cupones`, `/admin/reportes`, `/admin/comisiones`.
- `/403` y ruta inexistente servida por SPA.

## 6. Recomendaciones

1. Permitir `GET /api/restaurants/{id}` para clientes y usuarios anonimos, manteniendo proteccion solo para update/delete.
2. Cambiar el error de entrega ajena de `409` a `403`.
3. Agregar busqueda textual por nombre en backend y UI.
4. Completar formularios visuales para horarios, perfil restaurante, comisiones y administracion de restaurantes.
5. Agregar seed de promociones activas para que la demo muestre datos.
6. Separar historial de repartidor de entregas activas en UI.
7. Agregar pruebas automatizadas E2E con Playwright o Cypress para validar DOM, redirecciones y formularios desde navegador real.
8. Limpiar warnings de `react-hooks/exhaustive-deps` antes de entrega final si el tiempo lo permite.

## 7. Veredicto

El sistema esta en estado funcional para demostrar el flujo principal del proyecto. La ruta critica cliente-restaurante-repartidor-cliente funciona correctamente y no requiere asignacion manual del administrador. Para una entrega mas pulida, conviene corregir el acceso publico al detalle de restaurante y enriquecer algunas pantallas administrativas/restaurante que hoy son minimas.
