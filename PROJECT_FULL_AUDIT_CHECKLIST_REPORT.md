# Auditoria Completa Del Proyecto Delivery

Fecha: 2026-06-20  
Backend auditado: `/home/armandoaguilar/Desktop/delevery_backend`  
Frontend auditado: `/home/armandoaguilar/Desktop/delevery_frontend`  
Resultado de validacion local:

- Backend: `./mvnw -q test` exitoso. Surefire reporta 47 tests, 0 fallos, 0 errores.
- Frontend: `npm run build` exitoso. Vite genero build productivo correctamente.

## Resumen Ejecutivo

El proyecto esta en estado **funcional y avanzado para entrega academica**. Cumple la mayoria de requisitos tecnicos y funcionales del checklist: backend y frontend separados, API REST, JWT, roles, arquitectura por capas, manejo global de errores, Docker, Nginx, PostgreSQL/PostGIS, Swagger, flujos por rol, carrito, pedidos, delivery, reclamos, reembolsos, cupones, fidelidad, calificaciones, reportes, imagenes y factura descargable.

Cumplimiento global estimado: **86%**.

La calificacion no llega mas arriba por cuatro razones principales:

- El seguimiento es REST por polling/manual refresh, no tiempo real con WebSockets/SSE.
- El pago es simulado con tarjeta, no pasarela externa real.
- La cobertura de pruebas es buena en modulos clave, pero no cubre todos los flujos con seguridad real, integracion PostGIS y E2E automatizado.
- Faltan algunos entregables academicos no codificados: diagrama ER formal, reporte de aportes individuales y video de despliegue.

## Parte 1 - Backend + Frontend + API

Estado: ✅ Cumple

Evidencia:

- Backend Spring Boot en `src/main/java/sv/edu/uca/delivery/backend`.
- Frontend React/Vite en `/home/armandoaguilar/Desktop/delevery_frontend/src`.
- Cliente HTTP centralizado en `/home/armandoaguilar/Desktop/delevery_frontend/src/api/client.ts`.
- Variables de API frontend con `VITE_API_BASE_URL=/api`.
- Nginx enruta `/api/` hacia backend en `deploy/nginx/default.conf` y `nginx.conf.template`.

Explicacion:

El frontend consume endpoints REST mediante `fetch` centralizado; no hay evidencia de acceso directo a base de datos desde React. La base se usa exclusivamente desde backend mediante JPA/JdbcTemplate/Flyway.

Riesgos:

- El frontend esta concentrado casi completamente en un `App.tsx` grande. Funciona, pero reduce mantenibilidad.

Recomendacion:

- Dividir frontend por modulos: `auth`, `cliente`, `restaurante`, `repartidor`, `admin`, `shared`.

## Parte 2 - Logica De Negocio

Estado: ✅ Cumple

Evidencia:

- Pedidos: `OrderService`, `OrderFactory`, `OrderController`.
- Carrito: `CartService`, `CartController`.
- Delivery: `DeliveryService`, `DeliveryEstimateService`, `DeliveryController`.
- Reclamos: `ComplaintService`, `ComplaintController`.
- Cupones: `CouponService`, `CouponController`.
- Fidelidad: `LoyaltyService`, `LoyaltyController`.
- Restaurantes/productos: `RestaurantServiceImpl`, `ProductServiceImpl`.

Explicacion:

La logica principal no esta concentrada en controllers. Los controllers delegan a servicios y devuelven DTOs. Ejemplos claros:

- `CartService.addItem` valida usuario cliente, producto disponible, restaurante abierto y mezcla de restaurantes.
- `OrderService.createFromCart` calcula subtotal, impuestos, envio, propina, cupon, fidelidad y crea pago simulado.
- `OrderService.confirm` confirma pedido y llama `DeliveryService.assignAutomatically`.
- `DeliveryService.updateStatus` valida transiciones de estado.

Riesgos:

- El frontend aun contiene logica de presentacion y calculo estimado para mostrar totales dinamicos, aunque el backend recalcula el total real al crear el pedido. Esto es aceptable, pero debe mantenerse como informativo.

Recomendacion:

- Mantener backend como fuente de verdad para precios, envio, impuestos, descuentos y fidelidad.

## Parte 3 - Manejo De Excepciones

Estado: ✅ Cumple

Evidencia:

- `src/main/java/sv/edu/uca/delivery/backend/common/exception/GlobalExceptionHandler.java`.
- `BusinessException`.
- Excepciones por dominio: restaurante, producto, categoria, promocion, delivery, complaint, usuario.
- `server.error.include-stacktrace=never` en `application.yaml`.

Explicacion:

Existe respuesta uniforme `ApiErrorResponse` con timestamp, status, error, message, path y details. Maneja validaciones, credenciales invalidas, 404 de rutas inexistentes, conflictos y errores inesperados.

Riesgos:

- El handler global incluye el nombre de clase en errores 500 dentro de `details`. No expone stack trace, pero en produccion podria ocultarse incluso el tipo de excepcion.

Recomendacion:

- En perfil prod, remover detalles tecnicos de errores 500.

## Parte 4 - Metodos HTTP

Estado: ✅ Cumple

Evidencia:

- `GET` para consultas: restaurantes, productos, pedidos, tracking, reportes, cupones, usuarios.
- `POST` para creacion: auth login/register, carrito items, pedidos, reclamos, reviews, cupones, comisiones, imagenes.
- `PUT` para reemplazo/actualizacion completa: restaurantes, productos, categorias, horarios, direcciones, usuario.
- `PATCH` para acciones/estado: cancelar/confirmar/rechazar pedidos, disponibilidad, estados delivery, activar/desactivar.
- `DELETE` para eliminacion/vaciado: carrito, items, imagenes, direcciones.

Inconsistencias detectadas:

- `POST /api/deliveries/assign` sigue existiendo por compatibilidad, pero internamente responde `403` porque la asignacion manual esta deshabilitada. No rompe REST, pero puede confundir en Swagger si no se explica bien.

Recomendacion:

- Documentar en Swagger que ese endpoint no es flujo operativo y que la asignacion real ocurre al confirmar pedido.

## Parte 5 - Codigos HTTP

Estado: ✅ Cumple

Evidencia:

- `201 Created`: creacion de pedidos, carrito items, restaurantes, productos, reclamos, reviews, cupones, direcciones, comisiones.
- `204 No Content`: eliminar items, vaciar carrito, eliminar/desactivar recursos.
- `400 Bad Request`: validaciones Bean Validation y payloads invalidos.
- `401 Unauthorized`: `JsonAuthenticationEntryPoint`.
- `403 Forbidden`: `JsonAccessDeniedHandler` y reglas de negocio con `BusinessException`.
- `404 Not Found`: entidades ausentes y rutas inexistentes.
- `409 Conflict`: negocio conflictivo, por ejemplo restaurante cerrado, carrito de otro restaurante, pedido ya no cancelable.
- `500 Internal Server Error`: handler de fallback sin stack trace.

Riesgos:

- No se usa `422 Unprocessable Entity`; no es obligatorio si `400` y `409` estan bien separados.

Recomendacion:

- Mantener `400` para validacion sintactica y `409` para reglas de negocio.

## Parte 6 - Autenticacion Y Autorizacion

Estado: ✅ Cumple

Evidencia:

- `SecurityConfig`: JWT stateless, roles y reglas por endpoint.
- `JwtAuthenticationFilter`, `JwtService`, `AuthenticatedUserProvider`.
- `AuthController` y `AuthService`: login, registro, refresh, logout, me.
- `PasswordEncoder`: BCrypt.
- Roles: `ADMIN`, `CUSTOMER`, `RESTAURANT`, `DELIVERY`.

Explicacion:

Los endpoints privados requieren autenticacion. La autorizacion combina reglas de Spring Security con validaciones de servicio:

- Admin: `/api/admin/**`, `/api/reports/**`, `/api/coupons/**`, `/api/users/**`.
- Restaurante: gestion de restaurante/productos/categorias/promociones y pedidos propios.
- Repartidor: `/api/deliveries/**`.
- Cliente: carrito, pedidos, direcciones, reclamos, reviews y fidelidad.

Riesgos:

- Faltan mas tests de seguridad con filtros reales activos para todos los roles y rutas.
- El JWT tiene valor por defecto de desarrollo en `application.yaml`; en produccion debe ser obligatorio por variable.

Recomendacion:

- Agregar perfil `prod` que falle al arrancar si no existe `JWT_SECRET`.

## Parte 7 - Arquitectura

Estado: ✅ Cumple

Evidencia:

- Paquetes por dominio: `auth`, `user`, `restaurant`, `product`, `category`, `cart`, `order`, `delivery`, `complaint`, `coupon`, `loyalty`, `review`, `report`, `admin`, `media`.
- Capas: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `exception`, `config`, `security`.
- `open-in-view=false` en `application.yaml`.
- DTOs usados en controllers.
- Transacciones en servicios.

Explicacion:

La estructura es consistente y razonable para Spring Boot. Hay separacion de responsabilidades y uso de DTOs/mappers en modulos principales.

Riesgos:

- Algunos servicios usan `JdbcTemplate` con SQL nativo para PostGIS/reportes/admin. Es valido por rendimiento o funciones especificas, pero requiere mantener queries sincronizadas con migraciones.
- Frontend monolitico en `App.tsx`.

Recomendacion:

- Extraer componentes/paginas frontend y encapsular SQL nativo en repositorios dedicados o adapters.

## Parte 8 - Despliegue

Estado: ✅ Cumple parcial alto

Evidencia:

- Backend `Dockerfile`.
- Frontend `Dockerfile`.
- `docker-compose.yml` para local.
- `docker-compose.aws.yml` para EC2.
- `deploy/nginx/default.conf`.
- PostgreSQL/PostGIS con imagen `postgis/postgis:16-3.4`.
- Reporte `AWS_DEPLOYMENT_REPORT.md`.
- README documenta Docker, AWS EC2, variables y puertos.

Explicacion:

El proyecto esta dockerizado como stack de una instancia: Nginx, frontend, backend y PostGIS. Tambien tiene volumen para base y uploads.

Riesgos:

- No hay HTTPS configurado.
- No hay CI/CD versionado.
- `docker-compose.aws.yml` trae un `JWT_SECRET` default de desarrollo como fallback; debe reemplazarse siempre en `aws.env`.
- CORS default del compose contiene una IP antigua como fallback, aunque al usar Nginx por mismo origen esto no bloquea el flujo normal.

Recomendacion:

- Antes de produccion real: HTTPS con Certbot/Traefik, secretos obligatorios, backups de volumen Postgres, y CI/CD basico.

## Parte 9 - Funcionalidades De Servicio De Delivery

| # | Funcionalidad | Estado | Evidencia | Comentarios |
|---|---|---|---|---|
| 1 | Restaurantes administran productos, precios y horarios | ✅ | `RestaurantController`, `ProductController`, `CategoryController`, `RestaurantSchedulesPage`, `RestaurantProductsPage` | CRUD y horarios implementados; frontend usa modales para productos/categorias. |
| 2 | Usuarios buscan por ubicacion | ✅ | `GET /api/restaurants/nearby`, `RestaurantRepository.findNearby`, Leaflet en frontend | Usa PostGIS y mapa/pin para direcciones. |
| 3 | Usuarios buscan por categoria | ✅ | `GET /api/products/category/{categoryId}`, categorias y productos | Funciona por API; UI tiene catalogo por restaurante/categorias. |
| 4 | Usuarios buscan promociones | ✅ | `PromotionController`, `/api/promotions/active`, `/api/promotions/restaurant/{id}` | Promociones existen; depende de datos seed/creados. |
| 5 | Carrito de compras | ✅ | `CartController`, `CartService`, `CartPage` | Agregar, actualizar, eliminar, vaciar, subtotal y estimados. |
| 6 | Tiempo estimado de entrega | ✅ | `DeliveryEstimateService`, `OrderResponse`, tracking | ETA por distancia, cantidad de items y hora pico. |
| 7 | Costo de envio segun distancia | ✅ | `DeliveryEstimateService`, `RestaurantRepository.distanceKmBetweenRestaurantAndAddress` | Usa PostGIS y fallback documentado de 4 km. |
| 8 | Demanda u horario pico | ✅ | `DeliveryEstimateService.isPeakDemand` | Multiplicador 1.25 y ETA adicional en horarios pico. |
| 9 | Asignacion automatica del repartidor cercano | ✅ | `OrderService.confirm`, `DeliveryService.assignAutomatically`, `findNearestCandidateForOrder` | Confirmar pedido ofrece entrega al repartidor cercano; fallback al primero disponible. |
| 10 | Uso de geolocalizacion | ✅ | PostGIS `geography(Point,4326)`, `delivery_locations`, `restaurants.location`, `addresses.location` | Se restauró PostGIS y se usa para distancia/cercania. |
| 11 | Seguimiento en tiempo real del pedido | ⚠️ | `GET /api/orders/{id}/tracking`, frontend `TrackingPage` | Hay seguimiento REST apto para polling/manual refresh, no WebSockets/SSE. |
| 12 | Calificacion del restaurante | ✅ | `ReviewController`, `ReviewService`, modal desde pedidos | Solo pedidos entregados, evita duplicado por pedido. |
| 13 | Calificacion del repartidor | ✅ | `ReviewService` asocia delivery user si existe assignment | Integrado en mismo flujo de review. |
| 14 | Cancelacion antes de confirmacion sin cargo | ✅ | `OrderService.cancel` permite solo `CREATED` | Regla implementada con `409` si ya avanzo. |
| 15 | Propina al repartidor | ✅ | `CreateOrderFromCartRequest.tipAmount`, `OrderFactory`, frontend checkout | Se suma al total y reportes delivery suman propinas. |
| 16 | Pedidos agrupados para mismo repartidor | ⚠️ | Modelo historico `delivery_batches` en migraciones; servicio actual asigna solicitudes individuales | Se permite multiples entregas activas por usuario, pero no hay flujo explicito de agrupacion/batch operativo. |
| 17 | Historial de pedidos | ✅ | `GET /api/orders/my-history`, `OrdersPage` | Cliente ve pedidos realizados. |
| 18 | Facturacion | ✅ | `GET /api/orders/{id}/invoice`, icono de descarga en pedidos | Genera factura HTML descargable. |
| 19 | Reporte de restaurantes mas pedidos | ✅ | `GET /api/reports/restaurants/most-ordered` | Incluye conteo y revenue. |
| 20 | Notificacion de horario pico a restaurantes | ⚠️ | `peakDemand` en carrito/pedido/tracking | Backend calcula horario pico; UI cliente lo muestra en checkout/tracking. No hay alerta dedicada en dashboard restaurante. |
| 21 | Sistema de reclamos | ✅ | `ComplaintController`, `ComplaintService`, modal desde pedidos | Cliente reclama pedidos entregados; admin gestiona estados. |
| 22 | Sistema de reembolsos | ✅ | `Refund`, `RefundRepository`, `UpdateComplaintStatusRequest.refundType/refundAmount` | Admin decide parcial/total y comentario/resolucion. |
| 23 | Cupones | ✅ | `CouponController`, `CouponService`, admin UI | CRUD, porcentaje/monto fijo, vigencia, limite, minimo y aplicacion al pedido. |
| 24 | Programa de fidelidad | ✅ | `LoyaltyController`, `LoyaltyService`, checkout con puntos | Acumula puntos al entregar y canje total en compra. |

## Parte 10 - Validacion De Roles

### Administrador

Estado: ✅ Cumple

Puede:

- Gestionar usuarios: listar, activar/desactivar, CRUD admin en backend.
- Revisar reclamos y resolver con reembolso parcial/total.
- Configurar comisiones globales.
- Consultar reportes, cupones y restaurantes.

Evidencia:

- `UserController`, `AdminController`, `ComplaintController`, `ReportController`, `CouponController`.
- Frontend: `AdminUsersPage`, `AdminComplaintsPage`, `AdminCouponsPage`, `AdminReportsPage`, `AdminCommissionsPage`.

### Restaurante

Estado: ✅ Cumple

Puede:

- Administrar menu/productos/categorias.
- Administrar horarios.
- Aceptar/rechazar pedidos.
- Subir imagen de restaurante y producto.

Evidencia:

- `RestaurantController`, `ProductController`, `CategoryController`, `OrderController.confirm/reject`.
- Frontend: `RestaurantProfilePage`, `RestaurantProductsPage`, `RestaurantSchedulesPage`, `RestaurantOrdersPage`.

### Repartidor

Estado: ✅ Cumple

Puede:

- Recibir solicitudes de delivery.
- Aceptar/rechazar solicitudes.
- Ver entregas activas e historial.
- Actualizar estado.
- Actualizar ubicacion/disponibilidad.

Evidencia:

- `DeliveryController`: `/requests`, `/accept`, `/reject`, `/active`, `/history`, `/status`, `/location`, `/availability`, `/stats`.
- Frontend: `DeliveryRequestsPage`, `DeliveryActivePage`, `DeliveryHistoryPage`, `DeliveryProfilePage`.

## Parte 11 - Escalabilidad Y Builder Pattern

Estado: ✅ Cumple parcial

Evidencia:

- `OrderFactory.fromCart` centraliza la creacion de pedidos complejos.
- `OrderService.createFromCart` calcula atributos opcionales: impuestos, envio, ETA, propina, descuento, cupon, fidelidad, distancia y multiplicador de demanda.
- Entidades usan UUID v7 para IDs transaccionales.

Explicacion:

No hay un Builder clasico con API fluida, pero `OrderFactory` cumple la intencion principal: evitar constructores largos y concentrar el ensamblado de `Order`.

Riesgos:

- Para alto volumen real, faltan pruebas de carga, colas, idempotencia fuerte y tuning de DB.

Recomendacion:

- Evolucionar `OrderFactory` hacia `OrderBuilder` o comandos inmutables si crece el numero de atributos.
- Agregar pruebas de concurrencia para carrito/pedido/asignacion.

## Parte 12 - Integracion De Pagos

Estado: ⚠️ Parcial

Evidencia:

- Tabla `payments`.
- `OrderService.createFromCart` crea `Payment` con `PaymentStatus.PAID`.
- Frontend checkout muestra pago simulado con tarjeta.
- Migracion `V16__remove_paypal_checkout_support.sql` remueve soporte PayPal y deja pagos simulados/Stripe como constraints.

Explicacion:

Por decision reciente se retiro PayPal y se mantiene simulacion de pago con tarjeta. Para un proyecto academico puede ser suficiente si el requisito permite simulacion; si se exige pasarela real, no cumple completamente.

Riesgos:

- No hay validacion real con proveedor externo ni conciliacion.
- La tarjeta no se procesa ni se almacena, lo cual es seguro para demo, pero no es pago real.

Recomendacion:

- Si el catedratico exige pago real, integrar Stripe Checkout o PayPal Sandbox en una rama separada y sin hardcodear secretos.

## Parte 13 - Entregables

| Entregable | Estado | Evidencia / comentario |
|---|---|---|
| Repositorio backend | ✅ | Repositorio local backend y Git remoto usado previamente. |
| Repositorio frontend | ✅ | Repositorio local frontend y Git remoto usado previamente. |
| Swagger/OpenAPI | ✅ | `OpenApiConfig`, `springdoc`, `/swagger-ui.html`, `/v3/api-docs`. |
| Documentacion tecnica | ✅ | `README.md`, reportes por modulo y despliegue. |
| URL desplegada | ✅ | Despliegue EC2 documentado; URL usada: `http://100.59.192.85/`. |
| Diagrama Entidad-Relacion | ❓ | No se encontro archivo claro `.drawio`, imagen ERD o DBML. |
| Reporte de aportes individuales | ❓ | No se encontro documento especifico de aportes por integrante. |
| Video del despliegue | ❓ | No se encontro archivo/link de video en repositorios. |

Recomendacion:

- Generar `docs/ERD.md` o `.drawio`, `CONTRIBUTIONS.md` y un link a video demo/despliegue antes de entregar.

## Parte 14 - Calidad General

Estado: ✅ Cumple parcial alto

Fortalezas:

- Arquitectura por dominios y capas.
- DTOs y validaciones Bean Validation.
- Flyway con migraciones incrementales.
- Docker y Nginx listos.
- PostGIS usado correctamente para distancia/cercania.
- Reportes y documentacion abundante.
- Tests automatizados verdes.

Debilidades:

- Frontend concentrado en un solo archivo grande.
- Algunos reportes antiguos quedaron desactualizados frente al estado actual.
- SQL nativo en `JdbcTemplate` aumenta acoplamiento con PostgreSQL/PostGIS.
- Falta E2E automatizado.
- No hay perfil prod endurecido.

Recomendacion:

- Refactor gradual del frontend y agregar pruebas Playwright/Cypress.

## Parte 15 - Seguridad

Estado: ⚠️ Parcial alto

Evidencia positiva:

- JWT y refresh tokens.
- BCrypt para passwords.
- Seguridad stateless.
- Handlers JSON para 401/403.
- Roles y proteccion por endpoint.
- Validaciones de ownership en servicios.
- No se devuelven stack traces.
- CORS configurable por variable.

Riesgos encontrados:

- `JWT_SECRET` tiene fallback de desarrollo en `application.yaml` y `docker-compose.aws.yml`.
- Existe `.env` local en el entorno; debe mantenerse fuera de Git y no compartirse.
- Swagger queda publico; util para entrega, pero en produccion podria restringirse.
- Falta cobertura amplia de tests con filtros de seguridad reales.
- Frontend usa `localStorage` para tokens; aceptable para demo, mas riesgoso que cookies httpOnly en produccion.

Inyeccion SQL:

- Las queries nativas revisadas usan parametros (`?`, `:param`) y no concatenan input directamente. Riesgo bajo.

Recomendacion:

- Perfil prod sin defaults inseguros, secretos rotados, Swagger protegido si se despliega publicamente, y tests 401/403 por rol.

## Resultado Final

| Requisito | Estado | Observaciones |
|---|---|---|
| Backend + Frontend conectados por API | ✅ | Separados y comunicados por `/api`; Nginx/proxy configurado. |
| Logica de negocio | ✅ | Principalmente en servicios; backend es fuente de verdad. |
| Manejo de excepciones | ✅ | Handler global uniforme y sin stack traces. |
| Metodos HTTP | ✅ | Uso REST correcto en general. |
| Codigos HTTP | ✅ | 200/201/204/400/401/403/404/409/500 cubiertos. |
| Autenticacion | ✅ | JWT, refresh, logout, BCrypt. |
| Autorizacion | ✅ | Roles y ownership en servicios; faltan mas tests con filtros reales. |
| Arquitectura | ✅ | Capas y modulos claros; frontend requiere modularizacion. |
| Despliegue | ⚠️ | Docker/EC2/Nginx/PostGIS listo; falta HTTPS/CI/CD/perfil prod estricto. |
| Funcionalidades del delivery | ✅ | Muy completo; tiempo real y batching quedan parciales. |
| Roles | ✅ | Admin, cliente, restaurante y repartidor funcionales. |
| Escalabilidad Builder | ⚠️ | `OrderFactory` cumple intencion; falta carga/concurrencia real. |
| Integracion de pagos | ⚠️ | Pago simulado, no pasarela externa. |
| Entregables | ⚠️ | Falta verificar/generar ERD, aportes individuales y video. |
| Seguridad | ⚠️ | Buena base; endurecer secretos, Swagger prod y pruebas. |
| Calidad general | ✅ | Proyecto mantenible y funcional, con deuda moderada en frontend/tests. |

## 10 Problemas Mas Importantes

1. No hay seguimiento realtime estricto; solo REST polling/manual refresh.
2. Pago real externo no implementado; solo simulacion con tarjeta.
3. Falta diagrama ER formal verificable.
4. Falta reporte de aportes individuales.
5. Falta video/link de demostracion/despliegue.
6. Falta HTTPS y endurecimiento de secretos para produccion.
7. Falta CI/CD.
8. Falta E2E automatizado que pruebe flujos completos desde navegador.
9. Frontend esta demasiado concentrado en `App.tsx`.
10. Faltan pruebas de integracion con PostGIS/Flyway/Testcontainers y seguridad real por rol.

## Plan De Accion Para Subir Nota Rapido

1. Generar ERD: usar esquema Flyway actual y crear `docs/ERD.md` con imagen o diagrama Mermaid.
2. Crear `CONTRIBUTIONS.md` con aportes por integrante, ramas/modulos y evidencia.
3. Grabar video demo de 5 a 8 minutos: login por rol, pedido, confirmacion, delivery, tracking, factura, reclamo, admin.
4. Agregar Playwright con 4 E2E minimos: cliente compra, restaurante confirma, repartidor entrega, admin resuelve reclamo.
5. Proteger perfil produccion: sin JWT default, `DEV_SEED_ENABLED=false`, Swagger opcional, CORS de dominio real.
6. Agregar HTTPS en EC2 con Nginx/Certbot o preparar guia.
7. Refactor frontend por carpetas sin cambiar UI: `pages`, `components`, `api`, `hooks`.
8. Agregar tests de seguridad con Spring Security activo para 401/403 por rol.
9. Documentar explicitamente que el tracking es REST por decision de alcance y no WebSockets.
10. Si el catedratico exige pasarela real, integrar Stripe/PayPal Sandbox como extra opcional; si no, dejar pago simulado documentado.

## Conclusion

El proyecto **si puede presentarse como una version funcional del sistema Delivery**. Tiene una base tecnica seria: Spring Boot, React, JWT, roles, PostGIS, Docker, Nginx, Flyway, Swagger y flujos completos de negocio.

La mejor inversion antes de entrega no es reescribir funcionalidad, sino cerrar entregables academicos, pruebas E2E, documentacion visual y endurecimiento minimo de produccion.
