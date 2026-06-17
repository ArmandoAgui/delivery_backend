# Project Code Alignment Report

## 1. Funcionalidades Implementadas O Corregidas

- El flujo de pedido quedo alineado con la rubrica: cliente crea pedido, restaurante confirma y el backend asigna repartidor automaticamente.
- La asignacion manual cotidiana de delivery quedo bloqueada para API publica; el frontend ya no la expone.
- El carrito y los pedidos ahora incluyen estimaciones de envio: costo estimado, ETA, distancia y bandera de horario pico.
- El tracking REST fue ampliado con datos de delivery asignado, repartidor, ETA, costo de envio, distancia y horario pico.
- Se agrego busqueda de restaurantes cercanos con PostGIS mediante `/api/restaurants/nearby`.
- Se mantiene el flujo sin WebSockets, mapas, rutas GPS ni navegacion paso a paso.

## 2. Cambios Backend

- `PATCH /api/orders/{id}/confirm` ahora confirma el pedido y llama internamente a la asignacion automatica de repartidor.
- `DeliveryService` separa la asignacion interna `assignAutomatically(Order order)` del endpoint publico manual.
- `POST /api/deliveries/assign` queda deshabilitado para operacion normal y devuelve error indicando que se confirme el pedido.
- Se agrego `DeliveryEstimateService` para calcular costo de envio, ETA, distancia y multiplicador de demanda.
- `OrderFactory` ahora recibe una estimacion de delivery para construir pedidos sin setters dispersos.
- `Order`, `OrderResponse`, `CartResponse` y `OrderTrackingResponse` fueron ampliados con campos de estimacion.
- Se agrego migracion `V10__order_delivery_estimates.sql` para asegurar columnas de estimacion.
- Se agregaron consultas PostGIS en `RestaurantRepository` para distancia restaurante-direccion y restaurantes cercanos.

## 3. Cambios Frontend

- Se agrego `react-router-dom` y rutas reales por rol.
- Rutas publicas: `/`, `/login`, `/register`.
- Rutas cliente: `/cliente`, `/cliente/restaurantes`, `/cliente/restaurantes/:id`, `/cliente/carrito`, `/cliente/checkout`, `/cliente/pedidos`, `/cliente/pedidos/:id`, `/cliente/tracking/:id`, `/cliente/direcciones`, `/cliente/perfil`, `/cliente/fidelidad`, `/cliente/reclamos`, `/cliente/calificaciones`.
- Rutas restaurante: `/restaurante`, `/restaurante/perfil`, `/restaurante/menu`, `/restaurante/productos`, `/restaurante/horarios`, `/restaurante/pedidos`, `/restaurante/pedidos/:id`.
- Rutas repartidor: `/repartidor`, `/repartidor/entregas`, `/repartidor/entregas/:id`, `/repartidor/historial`.
- Rutas admin: `/admin`, `/admin/usuarios`, `/admin/reclamos`, `/admin/cupones`, `/admin/reportes`, `/admin/comisiones`.
- Errores: `/403` y fallback `404`.
- Se elimino del frontend el formulario de asignacion manual de repartidor.
- El checkout muestra subtotal, envio estimado, ETA, distancia, propina, cupon y aviso de horario pico.

## 4. Ajustes PostGIS

- Se conserva `GEOGRAPHY(Point, 4326)` para restaurantes, direcciones y ubicaciones de repartidores.
- La distancia restaurante-direccion se calcula con `ST_Distance`.
- La busqueda cercana usa `ST_DWithin` y ordena por `ST_Distance`.
- Si no hay coordenadas suficientes, el backend usa fallback documentado: distancia estimada de 4 km, costo base y ETA conservador.

## 5. Flujo Final Del Pedido

1. Cliente agrega productos al carrito.
2. Cliente crea pedido desde checkout con direccion, cupon, propina y notas.
3. Restaurante consulta pedidos recibidos.
4. Restaurante confirma el pedido.
5. Backend asigna automaticamente el repartidor disponible, favoreciendo cercania PostGIS.
6. Repartidor ve el pedido en `/repartidor/entregas`.
7. Repartidor avanza `ASSIGNED -> PICKED_UP -> ON_THE_WAY -> DELIVERED`.
8. Cliente consulta tracking con endpoint REST.
9. Al entregar, el cliente puede crear reclamo, calificar y acumular puntos.

## 6. Endpoints Añadidos O Modificados

- `PATCH /api/orders/{id}/confirm`: confirma y asigna delivery automaticamente.
- `GET /api/orders/{id}/tracking`: incluye delivery, ETA, fee, distancia y peak demand.
- `GET /api/cart`: incluye `estimatedDeliveryFee`, `estimatedDeliveryMinutes`, `peakDemand` y `distanceKm`.
- `GET /api/restaurants/nearby?lat=&lng=&radiusKm=`: busqueda cercana con PostGIS.
- `POST /api/deliveries/assign`: deshabilitado para flujo manual cotidiano.

## 7. Componentes Reutilizados

- Cliente HTTP con JWT/refresh token.
- Sesion en `localStorage`.
- Layout responsive existente.
- Cards, tablas, estados de carga/error/exito.
- Servicios backend existentes de auth, carrito, ordenes, delivery, reclamos, cupones, fidelidad, reviews y reportes.

## 8. Pendientes Tecnicos

- El frontend mantiene algunos warnings de `react-hooks/exhaustive-deps`; no bloquean build ni lint, pero pueden limpiarse con hooks estables o separando loaders.
- Las paginas de restaurante perfil/horarios y detalle exacto de entrega usan vistas funcionales minimas; se pueden enriquecer con formularios dedicados.
- `POST /api/deliveries/assign` sigue existiendo para compatibilidad de contrato, pero responde bloqueado; si el equipo quiere eliminarlo por completo, debe actualizar Swagger/tests/clientes.

## 9. Matriz De Cumplimiento

| Requisito | Estado |
| --- | --- |
| Auth JWT y roles | Cumplido |
| CRUD usuarios/admin | Cumplido |
| Restaurantes, horarios y estado | Parcial funcional |
| Productos/menu/categorias | Cumplido |
| Carrito con subtotal | Cumplido |
| Pedido desde carrito | Cumplido |
| Propina, impuestos, descuento, envio y total | Cumplido |
| Costo de envio por distancia/horario pico | Cumplido |
| ETA de entrega | Cumplido |
| Confirmacion restaurante | Cumplido |
| Asignacion automatica de repartidor | Cumplido |
| Seguimiento REST sin WebSockets | Cumplido |
| Reclamos y reembolsos | Cumplido |
| Cupones | Cumplido |
| Fidelidad | Cumplido |
| Calificaciones post-entrega | Cumplido |
| Reportes admin | Cumplido |
| Frontend con rutas por rol | Cumplido |
| PostGIS para cercania/distancia | Cumplido |
| Sin mapas/rutas GPS/WebSockets | Cumplido |

## 10. Evidencia

- Backend: `./mvnw test -q` ejecutado correctamente.
- Frontend: `npm run lint` ejecutado sin errores, con warnings de hooks.
- Frontend: `npm run build` ejecutado correctamente.
