# Delivery Role Fix Report

Fecha: 2026-06-17

## Objetivo

Auditar, corregir y completar el flujo del rol `DELIVERY` / Repartidor con uso de PostGIS, solicitudes aceptables/rechazables, reasignacion automatica y UI funcional.

## Hallazgos Principales

| Hallazgo | Causa | Correccion |
| --- | --- | --- |
| El backend asignaba directamente al repartidor al confirmar pedido | `assignAutomatically` creaba `ASSIGNED` sin permitir aceptar/rechazar | Se agrego estado `OFFERED` como solicitud pendiente |
| No existia rechazo con reasignacion | No habia tabla de rechazos ni exclusion de candidatos | Se agrego `delivery_assignment_rejections` y busqueda del siguiente repartidor por PostGIS |
| No existia perfil/disponibilidad del repartidor | Solo existian muestras de ubicacion historicas | Se agrego `delivery_profiles` con `is_available` |
| No habia endpoint para actualizar ubicacion actual | `delivery_locations` exigia asociacion a pedido/batch | Se permitio ubicacion sin orden y se agrego `PATCH /api/deliveries/location` |
| El frontend mostraba una sola lista de entregas | No separaba solicitudes, activas, historial o perfil | Se agregaron pantallas separadas por flujo |
| Faltaban estadisticas del repartidor | No habia endpoint agregado | Se agrego `GET /api/deliveries/stats` |

## Cambios Backend

### Migracion

Se agrego:

- `V11__delivery_requests_profiles_and_rejections.sql`

Incluye:

- Estado `OFFERED`.
- Estado `REJECTED`.
- Tabla `delivery_profiles`.
- Tabla `delivery_assignment_rejections`.
- Permitir ubicaciones actuales sin `order_id` o `delivery_batch_id`.
- Indices para disponibilidad y rechazos.

### Flujo De Solicitudes

Al confirmar un pedido:

1. El backend valida que el pedido pueda asignarse.
2. Busca el repartidor disponible mas cercano al restaurante usando PostGIS.
3. Crea `delivery_assignments.status = OFFERED`.
4. El repartidor ve la solicitud en `/api/deliveries/requests`.

Al aceptar:

- `PATCH /api/deliveries/{id}/accept`
- Cambia `OFFERED -> ASSIGNED`.
- Permite avanzar estados.

Al rechazar:

- `PATCH /api/deliveries/{id}/reject`
- Registra rechazo en `delivery_assignment_rejections`.
- Excluye ese repartidor de futuros candidatos para esa orden.
- Reofrece al siguiente repartidor cercano si existe.
- Si no hay candidato, deja la asignacion como `REJECTED` y el pedido permanece confirmado.

### PostGIS

La seleccion de candidato usa:

- Ultima ubicacion del repartidor en `delivery_locations.location`.
- Ubicacion del restaurante en `restaurants.location`.
- `ST_Distance(...)` para ordenar por cercania.
- Fallback a primer repartidor disponible no rechazado si no hay ubicaciones suficientes.

## Endpoints Agregados O Completados

| Endpoint | Metodo | Uso |
| --- | --- | --- |
| `/api/deliveries/requests` | `GET` | Solicitudes pendientes del repartidor autenticado |
| `/api/deliveries/active` | `GET` | Entregas activas aceptadas |
| `/api/deliveries/history` | `GET` | Historial de entregas |
| `/api/deliveries/{id}/accept` | `PATCH` | Aceptar solicitud |
| `/api/deliveries/{id}/reject` | `PATCH` | Rechazar solicitud y reofrecer |
| `/api/deliveries/profile` | `GET` | Perfil, disponibilidad y ultima ubicacion |
| `/api/deliveries/location` | `PATCH` | Actualizar ubicacion lat/lng con PostGIS |
| `/api/deliveries/availability` | `PATCH` | Actualizar disponibilidad |
| `/api/deliveries/stats` | `GET` | Estadisticas propias |

Se mantiene:

- `/api/deliveries/my-orders`
- `/api/deliveries/{id}/status`
- `/api/deliveries/assign` bloqueado como flujo manual cotidiano.

## Cambios Frontend

Rutas nuevas:

- `/repartidor/perfil`
- `/repartidor/solicitudes`
- `/repartidor/entregas`
- `/repartidor/historial`
- `/repartidor/estadisticas`

La UI ahora permite:

- Ver solicitudes pendientes.
- Aceptar solicitud.
- Rechazar solicitud.
- Ver entregas activas.
- Avanzar estados `ASSIGNED -> PICKED_UP -> ON_THE_WAY -> DELIVERED`.
- Ver historial.
- Actualizar ubicacion manual con latitud/longitud.
- Cambiar disponibilidad.
- Ver estadisticas de entregas, rechazos, ganancias estimadas y propinas.

## Flujo E2E Probado

| Paso | Resultado |
| --- | --- |
| Login cliente | `200 OK` |
| Login restaurante | `200 OK` |
| Login repartidor cercano | `200 OK` |
| Login repartidor secundario | `200 OK` |
| Actualizar ubicacion repartidor cercano | `200 OK` |
| Actualizar ubicacion repartidor secundario | `200 OK` |
| Cliente crea pedido | `201 Created` |
| Restaurante confirma pedido | `200 OK` |
| Repartidor cercano recibe solicitud | `200 OK`, `OFFERED` |
| Repartidor secundario no ve solicitud inicial | `200 OK`, lista vacia |
| Repartidor cercano intenta saltar directo a `DELIVERED` | `409 Conflict` esperado |
| Repartidor cercano rechaza | `200 OK` |
| Repartidor cercano deja de ver solicitud | `200 OK`, lista vacia |
| Repartidor secundario recibe solicitud | `200 OK`, `OFFERED` |
| Repartidor secundario acepta | `200 OK`, `ASSIGNED` |
| Repartidor cercano intenta modificar entrega ajena | `403 Forbidden` |
| Repartidor secundario avanza a `PICKED_UP` | `200 OK` |
| Repartidor secundario avanza a `ON_THE_WAY` | `200 OK` |
| Repartidor secundario avanza a `DELIVERED` | `200 OK` |
| Cliente consulta tracking final | `200 OK`, `DELIVERED` |
| Repartidor intenta acceder a usuarios/admin | `403 Forbidden` |

## Evidencia De Validacion

| Comando | Resultado |
| --- | --- |
| `./mvnw test -q` | Exitoso |
| `./mvnw spring-boot:run` | Exitoso, Flyway validó 11 migraciones |
| `npm run build` | Exitoso |
| `npm run lint` | Exitoso con warnings existentes de hooks |
| `GET /v3/api-docs` | Expone endpoints nuevos de repartidor |

## Decisiones Tecnicas

- Se usa `OFFERED` como solicitud pendiente para no crear una entidad paralela innecesaria.
- Se conserva una sola asignacion activa por orden, pero se registran rechazos en tabla separada.
- Se permite que un repartidor tenga multiples solicitudes o entregas activas.
- La ubicacion se registra manualmente por lat/lng; no se usan APIs externas.
- No se implementan mapas, rutas GPS, navegacion ni WebSockets.

## Pendientes Reales

- Si no hay siguiente repartidor al rechazar, el pedido queda confirmado y la asignacion queda `REJECTED`; podria agregarse un endpoint/admin job para reintentar asignacion cuando entre un nuevo repartidor disponible.
- No hay expiracion automatica de solicitudes `OFFERED`; podria agregarse con una tarea programada.
- No existe promedio de calificacion del repartidor en estadisticas porque el reporte actual no agrega reviews por repartidor.
- Los warnings de `react-hooks/exhaustive-deps` siguen presentes, sin romper build.

