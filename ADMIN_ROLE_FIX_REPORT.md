# Admin Role Fix Report

Fecha: 2026-06-18

## Objetivo

Auditar, corregir y completar el flujo del rol `ADMIN` para que pueda consultar usuarios, activar/desactivar cuentas, gestionar reclamos, cupones, comisiones globales y reportes sin intervenir en la operacion diaria de delivery.

## Cambios Backend

- `UserResponse` ahora expone `active` y `createdAt` para administracion.
- `POST /api/users` y `PUT /api/users/{id}` quedan bloqueados para admin con `403`; el administrador no modifica datos personales ni roles.
- `PUT /api/users/me` usa `updateProfile` y bloquea cambios de rol enviados manualmente.
- `DELETE /api/users/{id}` evita que un admin se desactive a si mismo.
- Se agrego `PATCH /api/users/{id}/activate`.
- `ComplaintResponse` ahora incluye cliente, correo, restaurante y estado del pedido asociado.
- `PATCH /api/complaints/{id}/status` acepta `resolution`, `refundType` (`NONE`, `PARTIAL`, `TOTAL`) y `refundAmount` para que admin decida reembolso parcial, total o sin reembolso.
- Cupones validan codigo unico al editar, porcentaje maximo de 100%, fechas coherentes y bloqueo de activacion de cupon vencido.
- Comisiones ahora son globales en `platform_commissions`; aplican el mismo porcentaje a todos los restaurantes y validan rango de fechas.
- Se agregaron reportes administrativos:
  - `GET /api/reports/orders/by-status`
  - `GET /api/reports/complaints/by-status`
  - `GET /api/reports/users/by-role`
  - `GET /api/reports/deliveries/top`
  - `GET /api/reports/products/top`
- Se agrego `GET /api/reports/restaurants/commissions` con ingreso, porcentaje global vigente y comision generada por restaurante.
- `GET /api/reports/admin-summary` ahora incluye `openComplaints` y `estimatedCommissions`.
- Se mantuvo bloqueado el flujo operativo de asignacion manual de delivery para frontend/admin.

## Cambios Frontend

- Se reemplazo el panel admin de JSON crudo por paginas funcionales.
- Nuevas/actualizadas vistas admin:
  - `/admin/usuarios`
  - `/admin/restaurantes`
  - `/admin/reclamos`
  - `/admin/cupones`
  - `/admin/reportes`
  - `/admin/comisiones`
- Usuarios: busqueda, filtro por rol y activar/desactivar. Se retiro edicion de datos personales y cambio de rol por regla de negocio.
- Restaurantes: listado, busqueda y desactivacion supervisada.
- Reclamos: filtro por estado, detalle contextual, tomar/resolver/rechazar con campo de comentario inline y selector de reembolso total, parcial o ninguno.
- Cupones: crear, editar, activar y desactivar.
- Reportes: tarjetas metricas, tablas de negocio y comision generada por restaurante.
- Comisiones: formulario de porcentaje global fijo e historial.
- Se corrigio el cliente frontend para usar los nombres reales de campos devueltos por backend.

## Endpoints Probados

| Endpoint | Rol probado | Resultado |
| --- | --- | --- |
| `POST /api/auth/login` | ADMIN | `200`, login correcto |
| `POST /api/auth/login` | CUSTOMER | `200`, login correcto |
| `GET /api/auth/me` | ADMIN | `200` |
| `GET /api/users` | ADMIN | `200` |
| `GET /api/users` | CUSTOMER | `403` |
| `GET /api/restaurants` | ADMIN | `200` |
| `GET /api/complaints` | ADMIN | `200` |
| `GET /api/coupons` | ADMIN | `200` |
| `GET /api/coupons` | CUSTOMER | `403` |
| `POST /api/coupons` | ADMIN | `201` |
| `PATCH /api/coupons/{id}/deactivate` | ADMIN | `200` |
| `GET /api/admin/commissions` | ADMIN | `200` |
| `GET /api/admin/commissions` | CUSTOMER | `403` |
| `POST /api/admin/commissions` | ADMIN | `201` |
| `GET /api/reports/restaurants/commissions` | ADMIN | `200` |
| `GET /api/reports/admin-summary` | ADMIN | `200` |
| `GET /api/reports/admin-summary` | CUSTOMER | `403` |
| `GET /api/reports/restaurants/most-ordered` | ADMIN | `200` |
| `GET /api/reports/orders/by-status` | ADMIN | `200` |
| `GET /api/reports/complaints/by-status` | ADMIN | `200` |
| `GET /api/reports/users/by-role` | ADMIN | `200` |
| `GET /api/reports/deliveries/top` | ADMIN | `200` |
| `GET /api/reports/products/top` | ADMIN | `200` |
| `POST /api/users` | ADMIN | `403`, bloqueado por regla de negocio |
| `PUT /api/users/{id}` | ADMIN | `403`, bloqueado por regla de negocio |
| `PATCH /api/users/{id}/activate` | ADMIN | `200` |
| `POST /api/complaints` | CUSTOMER | `201`, reclamo de prueba creado |
| `PATCH /api/complaints/{id}/status` | ADMIN | `200`, `OPEN -> IN_PROGRESS` |
| `PATCH /api/complaints/{id}/status` con reembolso parcial | ADMIN | `200`, refund `APPROVED` por `$1.25` |

## Validacion de Estadisticas

- `GET /api/reports/admin-summary` devolvio `revenue = 182.82` y `estimatedCommissions = 21.9384` con comision global vigente de `12%`.
- Calculo verificado: `182.82 * 0.12 = 21.9384`.
- `GET /api/reports/restaurants/commissions` devuelve por restaurante: ordenes, ingresos, porcentaje global aplicado y monto de comision.
- Ejemplo validado:
  - Restaurante Dev: `152.64 * 12% = 18.3168`.
  - Papas Pizzeria: `30.18 * 12% = 3.6216`.

## Validacion

- Backend: `./mvnw test -q` exitoso.
- Frontend: `npm run build` exitoso.
- Frontend lint: `npm run lint` exitoso con warnings existentes de `react-hooks/exhaustive-deps`, sin errores.
- Backend local reiniciado correctamente en `http://localhost:8080` con PostgreSQL local `localhost:5433`.

## Pendientes Menores

- Reducir warnings de hooks en frontend separando mejor efectos y acciones.
- Optimizar listados con paginacion real para bases grandes.
- Agregar endpoints de activacion de restaurantes si se requiere reabrir restaurantes desde admin; actualmente admin puede desactivar y listar activos.
