# Restaurant Role Fix Report

Fecha: 2026-06-17

## Objetivo

Auditar, corregir y completar el flujo del rol `RESTAURANT` para que un usuario restaurante pueda registrarse, iniciar sesion, crear su establecimiento, administrar horarios, categorias, productos/menu, pedidos recibidos, estadisticas y cuenta propia.

## Hallazgos Criticos

| Hallazgo | Causa | Correccion |
| --- | --- | --- |
| El registro publico siempre creaba `CUSTOMER` | `AuthService.register` ignoraba el rol enviado y forzaba `RoleName.CUSTOMER` | Se permite registrar `CUSTOMER`, `RESTAURANT` y `DELIVERY`; `ADMIN` queda bloqueado con `403` |
| El frontend no dejaba seleccionar rol al registrarse | El formulario enviaba siempre `role: 'CUSTOMER'` | Se agrego selector Cliente / Restaurante / Repartidor |
| El restaurante no tenia una ruta segura para consultar su establecimiento | El frontend listaba todos los restaurantes y elegia el primero | Se agrego `GET /api/restaurants/my` protegido para `RESTAURANT` |
| Un restaurante podia tener UI incompleta para crear establecimiento | `/restaurante/perfil` mostraba un dashboard generico | Se implemento formulario real de creacion/edicion de restaurante |
| Producto actualizado no revalidaba propietario | `ProductServiceImpl.update` no llamaba `requireAdminOrRestaurantOwner` | Se agrego validacion de propietario/admin antes de actualizar |
| Categoria desactivada no revalidaba propietario | `CategoryServiceImpl.softDelete` no llamaba `requireAdminOrRestaurantOwner` | Se agrego validacion de propietario/admin antes de desactivar |
| Restaurante desactivado no revalidaba propietario | `RestaurantServiceImpl.softDelete` no llamaba `requireOwner` | Se agrego validacion de propietario/admin antes de desactivar |

## Cambios Backend

- `POST /api/auth/register`
  - Ahora respeta el rol enviado si es `CUSTOMER`, `RESTAURANT` o `DELIVERY`.
  - Bloquea `ADMIN` con `403 Forbidden`.
  - El JWT conserva claim `role`.

- `GET /api/restaurants/my`
  - Nuevo endpoint protegido para obtener el restaurante del usuario `RESTAURANT` autenticado.
  - Devuelve `404` si el restaurante aun no existe.

- Seguridad
  - `GET /api/restaurants/my` requiere rol `RESTAURANT`.
  - Se reforzo validacion de propietario/admin en restaurantes, categorias y productos.

## Cambios Frontend

- Registro:
  - Selector de tipo de cuenta: Cliente, Restaurante, Repartidor.
  - Admin no aparece como opcion.
  - Despues de registrar/loguear, redirige segun rol.

- Restaurante:
  - `/restaurante/perfil`
    - Crear restaurante.
    - Editar restaurante.
    - Editar datos de cuenta.
    - Cambiar password opcional.
    - Capturar coordenadas lat/lng para PostGIS.
  - `/restaurante/horarios`
    - Guardar horarios por dia.
    - Marcar dias cerrados.
  - `/restaurante/productos` y `/restaurante/menu`
    - Crear categorias.
    - Editar/desactivar categorias.
    - Crear productos.
    - Editar productos.
    - Activar/desactivar disponibilidad.
    - Desactivar productos.
  - `/restaurante/pedidos`
    - Ver pedidos del restaurante.
    - Ver items, envio, propina, total y estado.
    - Confirmar o rechazar solo pedidos en `CREATED`.
    - Estadisticas locales: total pedidos, ventas, producto mas vendido y conteo por estado.

## Endpoints Probados

### Auth y Registro

| Endpoint | Resultado |
| --- | --- |
| `POST /api/auth/register` con `role=RESTAURANT` | `201 Created`, usuario con rol `RESTAURANT` |
| `POST /api/auth/register` con `role=ADMIN` | `403 Forbidden` |
| `POST /api/auth/login` restaurante seed | `200 OK` |
| `GET /api/auth/me` restaurante | `200 OK`, rol `RESTAURANT` |
| `PUT /api/users/me` restaurante | `200 OK` |

### Restaurante

| Endpoint | Resultado |
| --- | --- |
| `GET /api/restaurants/my` antes de crear | `404 Not Found` esperado |
| `POST /api/restaurants` | `201 Created` |
| `GET /api/restaurants/my` despues de crear | `200 OK` |
| `PUT /api/restaurants/{id}` | `200 OK` |
| `GET /api/restaurants/nearby?lat=&lng=&radiusKm=` | `200 OK`, consulta espacial PostGIS |

### Horarios

| Endpoint | Resultado |
| --- | --- |
| `PUT /api/restaurants/{id}/schedules` | `200 OK`, 7 horarios |
| `GET /api/restaurants/{id}/schedules` | `200 OK` |

### Categorias

| Endpoint | Resultado |
| --- | --- |
| `POST /api/categories` | `201 Created` |
| `PUT /api/categories/{id}` | `200 OK` |
| `PATCH /api/categories/{id}/deactivate` | Validado por propietario/admin |

### Productos

| Endpoint | Resultado |
| --- | --- |
| `POST /api/products` | `201 Created` |
| `PUT /api/products/{id}` | `200 OK` |
| `PATCH /api/products/{id}/availability` | `200 OK` |
| `PATCH /api/products/{id}/deactivate` | Validado por propietario/admin |

### Pedidos y Seguridad

| Endpoint | Resultado |
| --- | --- |
| `GET /api/orders/restaurant` | `200 OK` |
| `GET /api/users` usando rol restaurante | `403 Forbidden` |
| `PUT /api/restaurants/{id}` usando cliente | `403 Forbidden` |

## Flujo Validado

1. Registrar usuario con rol `RESTAURANT`.
2. Confirmar que `GET /api/auth/me` devuelve rol `RESTAURANT`.
3. Confirmar que `GET /api/restaurants/my` devuelve `404` antes de crear restaurante.
4. Crear restaurante asociado al usuario autenticado.
5. Consultar restaurante propio.
6. Actualizar restaurante.
7. Guardar horarios.
8. Crear categoria.
9. Editar categoria.
10. Crear producto.
11. Editar producto.
12. Cambiar disponibilidad.
13. Consultar pedidos del restaurante.
14. Confirmar bloqueo de acceso admin.
15. Confirmar bloqueo de modificacion de restaurante ajeno.
16. Confirmar consulta PostGIS de restaurantes cercanos.

## Validacion Tecnica

| Comando | Resultado |
| --- | --- |
| `./mvnw test -q` | Exitoso |
| `npm run build` | Exitoso |
| `npm run lint` | Exitoso con warnings existentes de `react-hooks/exhaustive-deps` |

## Pendientes Reales

- Los horarios se eliminan logicamente marcando el dia como `closed`; no existe endpoint dedicado para borrar un horario individual.
- La edicion de categoria/producto en frontend usa prompts simples para mantener el MVP ligero; puede mejorarse con modales o formularios inline.
- No existe subida real de imagen/logo en backend; el formulario no la expone para evitar simular funcionalidad inexistente.
- Las estadisticas de restaurante se calculan en frontend desde `GET /api/orders/restaurant`; si se requiere reporte productivo, conviene agregar endpoints agregados por restaurante.
- Persisten warnings de hooks en `src/App.tsx`, sin errores de build.

