# Estado actual de endpoints del backend Delivery

Fecha de prueba: 2026-06-16  
Rama: `feature/backend-integration-delivery`  
Base probada: `http://localhost:8080`

## Resumen Ejecutivo

El backend arranca y varios módulos ya responden correctamente, especialmente catálogo de restaurantes, categorías, productos, promociones, delivery y reclamos. Sin embargo, el proyecto todavía no está completo contra el alcance oficial: faltan endpoints reales para autenticación, pedidos, carrito, cupones, fidelidad, calificaciones, reportes y administración.

La seguridad sigue en modo desarrollo: `SecurityConfig` permite todas las peticiones y `AuthenticatedUserProvider` usa `X-Dev-User-Id` o `DEV_DELIVERY_USER_ID`. Esto permite probar módulos, pero todavía no cumple autenticación/autorización productiva con JWT, 401 y 403.

## Datos Generados Durante La Prueba

Se generaron datos mínimos usando la API local para validar flujos existentes:

| Dato | Resultado |
| --- | --- |
| Categoría de auditoría | Creada con `POST /categories`, id `8`, luego actualizada |
| Producto de auditoría | Creado con `POST /products`, id `019ed17a-dd36-7e93-b2fd-1f677a1d750f`, disponibilidad actualizada |
| Promoción de auditoría | Creada para `Pupuseria Demo`, id `4`, luego desactivada |
| Horarios de restaurante | Actualizados para restaurante `019e37b3-379d-7ed8-a9b5-213c8d36aafe` |
| Delivery assignment | `018f0000-0000-7000-8000-000000000601` avanzó de `ASSIGNED` a `PICKED_UP` |

No se creó un reclamo nuevo porque la orden entregada demo `018f0000-0000-7000-8000-000000000401` ya tiene un reclamo resuelto. La API respondió correctamente con error de negocio.

## Endpoints Encontrados

### Restaurantes

| Endpoint | Esperado | Obtenido actual | Estado |
| --- | --- | --- | --- |
| `POST /restaurants` | Crear restaurante, `201`; duplicado debe dar `409` | Duplicado por owner devolvió `409` con JSON controlado | Funcional |
| `GET /restaurants` | Listar restaurantes, `200` | `200`, lista con datos | Funcional |
| `GET /restaurants/{id}` | Obtener detalle, `200`; inexistente `404` | Existente `200`; inexistente `404` controlado | Funcional |
| `PUT /restaurants/{id}` | Actualizar, `200` | No probado en esta ronda; existe en controller | Pendiente de prueba completa |
| `PATCH /restaurants/{id}/deactivate` | Desactivar, `204` | No probado para evitar alterar datos activos | Pendiente de prueba completa |
| `GET /restaurants/open` | Listar abiertos, `200` | `200`, lista vacía porque restaurantes demo están cerrados | Funcional |
| `GET /restaurants/{id}/schedules` | Listar horarios, `200` | `200`, devuelve horarios actualizados | Funcional |
| `PUT /restaurants/{id}/schedules` | Reemplazar horarios, `200` | `200`, actualizó horarios | Funcional |

### Categorías

| Endpoint | Esperado | Obtenido actual | Estado |
| --- | --- | --- | --- |
| `POST /categories` | Crear categoría, `201`; validación `400` | `201` para payload válido; `400` con detalles para payload inválido | Funcional |
| `GET /categories` | Listar categorías, `200` | `200`, lista con datos | Funcional |
| `GET /categories/{id}` | Obtener categoría, `200`; inexistente `404` | `200` para categoría creada | Funcional |
| `PUT /categories/{id}` | Actualizar, `200` | `200` | Funcional |
| `PATCH /categories/{id}/deactivate` | Desactivar, `204` | No probado para no romper producto creado | Pendiente de prueba completa |
| `GET /categories/restaurant/{restaurantId}` | Categorías por restaurante, `200` | `200` | Funcional |

### Productos

| Endpoint | Esperado | Obtenido actual | Estado |
| --- | --- | --- | --- |
| `POST /products` | Crear producto, `201` | `201` | Funcional |
| `GET /products` | Listar productos, `200` | `200`, lista con productos y descuentos calculados | Funcional |
| `GET /products/{id}` | Obtener producto, `200`; inexistente `404` | `200` para producto creado | Funcional |
| `PUT /products/{id}` | Actualizar producto, `200` | `200` | Funcional |
| `PATCH /products/{id}/deactivate` | Desactivar, `204` | No probado para conservar dato de auditoría | Pendiente de prueba completa |
| `GET /products/restaurant/{restaurantId}` | Productos por restaurante, `200` | `200` | Funcional |
| `GET /products/available` | Productos disponibles, `200` | `200` | Funcional |
| `GET /products/category/{categoryId}` | Productos por categoría, `200` | Existe; no probado en esta ronda | Pendiente de prueba |
| `PATCH /products/{id}/availability` | Cambiar disponibilidad, `200` | `200` | Funcional |

### Promociones

| Endpoint | Esperado | Obtenido actual | Estado |
| --- | --- | --- | --- |
| `POST /promotions` | Crear promoción, `201`; si ya existe activa debería ser `409` | `201` en restaurante sin promo activa; `500` cuando ya existía promo activa | Parcial |
| `GET /promotions` | Listar promociones, `200` | `200` | Funcional |
| `GET /promotions/{id}` | Obtener promoción, `200` | `200` | Funcional |
| `PUT /promotions/{id}` | Actualizar promoción, `200` | Existe; no probado en esta ronda | Pendiente de prueba |
| `PATCH /promotions/{id}/deactivate` | Desactivar, `204` | `204` | Funcional |
| `GET /promotions/restaurant/{restaurantId}` | Promociones por restaurante, `200` | `200` | Funcional |
| `GET /promotions/active` | Promociones activas, `200` | `200` | Funcional |
| `PATCH /promotions/{id}/status` | Activar/desactivar, `200` | Existe; no probado con promo creada porque se probó `deactivate` | Pendiente de prueba |

Problema observado: `PromotionAlreadyExistsException` no está manejada por `GlobalExceptionHandler`, por eso expone `500` con stack trace. Debería devolver `409 Conflict` con respuesta uniforme.

### Usuarios

| Endpoint | Esperado | Obtenido actual | Estado |
| --- | --- | --- | --- |
| `GET /api/users/{id}` | Obtener usuario con rol, `200`; inexistente `404` | `500 LazyInitializationException` al leer `role.name` | Roto |

Causa probable: `UserServiceImpl.findById` usa `userRepository.findById(id)` y luego el mapper accede a `user.getRole().getName()` fuera de sesión. Se requiere `join fetch`, transacción read-only o mapper dentro de sesión.

Además, el módulo de usuarios solo expone consulta por id. No hay CRUD completo, perfil ni direcciones por API.

### Delivery

| Endpoint | Esperado | Obtenido actual | Estado |
| --- | --- | --- | --- |
| `POST /api/deliveries/assign` | Asignar repartidor, `201`; órdenes inválidas `409/400` | Orden cancelada devolvió `409` controlado | Funcional parcial |
| `GET /api/deliveries/my-orders` | Pedidos del repartidor autenticado, `200` | `200`; funciona usando `X-Dev-User-Id` o usuario dev | Funcional en modo dev |
| `PATCH /api/deliveries/{id}/status` | Cambiar estado según transición válida, `200`; transición inválida `409` | `ASSIGNED -> PICKED_UP` devolvió `200`; regresión a `ASSIGNED` devolvió `409` | Funcional |

Observación: el response de delivery no incluye dirección en texto plano ni datos del restaurante, aunque el requerimiento del repartidor pide ver dirección e información relevante del pedido.

### Reclamos y Reembolsos

| Endpoint | Esperado | Obtenido actual | Estado |
| --- | --- | --- | --- |
| `POST /api/complaints` | Crear reclamo sobre orden entregada propia, `201`; duplicado `400/409` | Intento sobre orden con reclamo existente devolvió `400` controlado | Funcional parcial |
| `GET /api/complaints` | Listar reclamos según rol, `200` | `200`, cliente ve su reclamo resuelto | Funcional |
| `GET /api/complaints/{id}` | Obtener reclamo, `200` | Existe; no probado porque no se creó uno nuevo | Pendiente de prueba |
| `PATCH /api/complaints/{id}/status` | Admin cambia estado, `200`; genera refund al resolver | Existe y hay evidencia de reclamo resuelto con refund aprobado | Funcional parcial |

Observación: el flujo existe, pero depende de `X-Dev-User-Id`; no hay auth real ni autorización por Spring Security.

## Endpoints Ausentes Probados

| Endpoint probado | Esperado por proyecto | Obtenido actual | Estado |
| --- | --- | --- | --- |
| `GET /api/orders` | API de pedidos: crear, confirmar, cancelar, historial, tracking | `404 NoResourceFoundException` con stack trace | No implementado |
| `GET /api/cart` | API de carrito | `404 NoResourceFoundException` con stack trace | No implementado |
| `GET /api/coupons` | CRUD/aplicación de cupones | `404 NoResourceFoundException` con stack trace | No implementado |
| `GET /api/loyalty` | Puntos y canje | `404 NoResourceFoundException` con stack trace | No implementado |
| `GET /api/reviews` | Calificaciones | `404 NoResourceFoundException` con stack trace | No implementado |
| `GET /api/reports` | Reportes administrativos | `404 NoResourceFoundException` con stack trace | No implementado |

Las tablas para varios de estos módulos existen en migraciones, pero no hay controllers/services expuestos todavía.

## Estado Contra Contexto Del Proyecto

| Módulo | Estado actual | Comentario |
| --- | --- | --- |
| Auth / Security | No implementado productivamente | No hay login, registro, JWT ni restricciones por rol; todo está `permitAll` |
| Usuarios | Inicial / roto | Solo `GET /api/users/{id}` y actualmente falla por lazy loading |
| Restaurantes | Avanzado | CRUD base y horarios existen |
| Categorías | Avanzado | CRUD base funciona |
| Productos / Menú | Avanzado | CRUD base, disponibilidad, asociación con restaurante/categoría funcionan |
| Promociones | Parcial | Funciona, pero errores de negocio no están manejados y devuelven `500` |
| Carrito | No implementado en API | Tablas existen, endpoints no |
| Pedidos | No implementado en API | Entidades/repos existen, pero no controller/service |
| Delivery | Parcial funcional | Asignación y estados existen; falta enriquecer response con dirección/restaurante |
| Seguimiento de pedido | No implementado como API dedicada | No hay endpoint REST de estado de pedido; no se requieren WebSockets |
| Reclamos / reembolsos | Parcial funcional | Flujo básico existe y refund simple aparece en datos |
| Cupones | No implementado en API | Hay tablas de redención, pero no CRUD/aplicación |
| Fidelidad | No implementado en API | Hay tablas, pero no endpoints |
| Calificaciones | No implementado en API | Tabla existe, pero no endpoints |
| Reportes | No implementado en API | No hay controller/service |
| Administración | No implementado como módulo | No hay endpoints admin dedicados ni protección por rol |

## Hallazgos Técnicos

| Severidad | Hallazgo | Evidencia | Recomendación |
| --- | --- | --- | --- |
| Alta | Seguridad deshabilitada | `SecurityConfig` usa `requests.anyRequest().permitAll()` | Implementar JWT, filtros, roles y respuestas 401/403 |
| Alta | Usuarios falla con `500` | `GET /api/users/{id}` lanza `LazyInitializationException` | Usar repository con `join fetch role` o `@Transactional(readOnly = true)` |
| Alta | Pedidos no tiene API | `/api/orders` devuelve `404` | Implementar controller/service de pedidos antes de integrar carrito/delivery completo |
| Alta | Carrito no tiene API | `/api/cart` devuelve `404` | Implementar flujo de carrito y creación de pedido desde carrito |
| Media | Promociones expone `500` para conflicto | `POST /promotions` con promo activa lanza `PromotionAlreadyExistsException` no manejada | Agregar handlers para excepciones de promotion/category/product |
| Media | 404 de rutas no existentes expone stack trace | `/api/orders`, `/api/cart`, etc. devuelven `NoResourceFoundException` con trace | Agregar handler global para `NoResourceFoundException` y desactivar stack traces en errores |
| Media | Inconsistencia de rutas | Algunos módulos usan `/api`, otros no | Definir prefijo único: idealmente `/api/...` |
| Media | Delivery response incompleto | No incluye dirección textual ni restaurante | Extender `DeliveryResponse` para repartidor |
| Media | Roles en inglés vs requerimiento en español | Código usa `CUSTOMER` y `DELIVERY`; requerimiento menciona `CLIENTE` y `REPARTIDOR` | Documentar equivalencias o renombrar con migración controlada |
| Baja | Imports/comentarios desordenados | `Product.java` tiene import duplicado y comentario informal | Limpieza menor en refactor |

## Resultados Destacados De Prueba

| Prueba | Resultado |
| --- | --- |
| `GET /restaurants` | `200`, respondió lista de restaurantes |
| `GET /restaurants/open` | `200`, lista vacía porque los restaurantes están cerrados |
| `POST /categories` válido | `201`, creó categoría |
| `POST /categories` inválido | `400`, validaciones correctas |
| `POST /products` válido | `201`, creó producto |
| `PATCH /products/{id}/availability` | `200`, cambió disponibilidad |
| `POST /promotions` en restaurante sin promoción activa | `201`, creó promoción |
| `POST /promotions` en restaurante con promoción activa | `500`, debería ser `409` |
| `PUT /restaurants/{id}/schedules` | `200`, actualizó horarios |
| `GET /api/users/{id}` | `500`, bug por lazy loading |
| `GET /api/deliveries/my-orders` | `200`, funciona en modo dev |
| `PATCH /api/deliveries/{id}/status` transición válida | `200`, avanzó a `PICKED_UP` |
| `PATCH /api/deliveries/{id}/status` transición inválida | `409`, correcto |
| `POST /api/complaints` duplicado | `400`, regla de negocio aplicada |

## Qué Falta Para Cumplir El Proyecto

1. Implementar autenticación real: login, password encoder, JWT, refresh si aplica, filtros y autorización por roles.
2. Completar usuarios: CRUD, perfil, direcciones e historial.
3. Implementar carrito con subtotal y validaciones de restaurante/producto.
4. Implementar pedidos con creación desde carrito, items, subtotal, impuestos, propina, descuentos, costo de envío y total.
5. Implementar confirmación/rechazo/cancelación de pedido y tracking REST por polling.
6. Conectar cupones al pedido y exponer CRUD de cupones.
7. Implementar fidelidad: acumulación, consulta y canje básico.
8. Implementar calificaciones de restaurante/repartidor con restricción de pedido entregado y sin duplicados.
9. Implementar reportes administrativos.
10. Implementar administración: usuarios, reclamos, comisiones y endpoints protegidos.
11. Normalizar rutas bajo `/api`.
12. Completar manejo global de excepciones para todos los módulos.

## Recomendación De Próximo Orden De Trabajo

1. Corregir errores actuales rápidos: `GET /api/users/{id}`, handlers de promotion/category/product y 404 global.
2. Activar una capa de auth mínima con JWT y roles, manteniendo `X-Dev-User-Id` solo bajo perfil `dev` si se desea.
3. Implementar pedidos porque desbloquea carrito, tracking, delivery, reclamos, cupones, loyalty y reviews.
4. Implementar carrito y creación de pedido desde carrito.
5. Enriquecer delivery response con dirección textual, restaurante y datos relevantes.
6. Implementar cupones, loyalty y reviews sobre pedidos entregados.
7. Cerrar reportes/admin y documentar OpenAPI si el proyecto lo adopta.

## Nota Sobre WebSockets Y GPS

No se marca como pendiente WebSockets, rutas GPS ni navegación. El seguimiento puede resolverse correctamente con endpoints REST consultados manualmente o por polling desde frontend.
