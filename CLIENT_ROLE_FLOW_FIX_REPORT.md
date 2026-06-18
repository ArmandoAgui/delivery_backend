# Client Role Flow Fix Report

Fecha: 2026-06-18

## 1. Resumen ejecutivo

El flujo del rol `CUSTOMER` quedo listo para demostracion academica. Se valido que el cliente puede iniciar sesion, gestionar direcciones con coordenadas compatibles con PostGIS, navegar restaurantes, agregar productos al carrito, ver costo de envio por distancia, hacer checkout con pago simulado, crear pedido, consultar tracking REST, crear reclamos, consultar fidelidad y calificar pedidos entregados.

No se integro Stripe ni geocoding externo. El pago de tarjeta es simulado y solo valida datos en frontend; no se guarda numero de tarjeta ni CVV.

## 2. Checklist funcional

| Funcionalidad | Estado Final | Observaciones |
| --- | --- | --- |
| Registro como cliente | ✅ | `POST /api/auth/register` mantiene rol `CUSTOMER` desde frontend. |
| Login cliente | ✅ | `POST /api/auth/login` probado con `cliente.dev@example.com`. |
| Gestión de direcciones | ✅ | Crear, editar, eliminar y marcar principal desde `/cliente/direcciones`. |
| Selección de ubicación con mapa | ✅ | Se agrego mini mapa sin dependencia externa; click actualiza latitud/longitud. |
| Guardado de coordenadas/PostGIS | ✅ | Backend guarda `GEOGRAPHY(Point, 4326)` y responde lat/lng. |
| Navegación de restaurantes | ✅ | Lista, busqueda y detalle de restaurante funcionales. |
| Búsqueda/filtros | ✅ | Busqueda por texto y restaurantes cercanos usando direccion principal/fallback. |
| Ver menú | ✅ | `GET /api/products/restaurant/{restaurantId}` en detalle. |
| Agregar al carrito | ✅ | `POST /api/cart/items` validado. |
| Modificar carrito | ✅ | Cambiar cantidad, quitar item y vaciar carrito. |
| Cálculo de envío por distancia | ✅ | Validado con `distanceKm = 0.38`, envio `$1.75`, ETA `24 min`. |
| Checkout | ✅ | Direccion, propina, cupon, notas, resumen y confirmacion. |
| Pago simulado con tarjeta | ✅ | Formulario con titular, tarjeta, expiracion y CVV. Validacion local, sin persistir tarjeta. |
| Pedidos realizados | ✅ | `GET /api/orders/my-history` en `/cliente/pedidos`. |
| Seguimiento REST | ✅ | `GET /api/orders/{id}/tracking` con boton actualizar. |
| Reclamos | ✅ | Crear y listar reclamos propios desde `/cliente/reclamos`. |
| Fidelidad | ✅ | Consulta puntos e historial; canje basico disponible. |
| Calificaciones | ✅ | Crear review sobre pedido entregado propio, con rating 1 a 5. |
| Seguridad por rol | ✅ | Cliente recibe `403` al intentar `GET /api/users`. Guards frontend restringen rutas por rol. |

## 3. Cambios realizados

### Frontend

- `/home/armandoaguilar/Desktop/delevery_frontend/src/App.tsx`
  - Se agrego `CoordinatePicker`, un mini mapa simple para capturar latitud/longitud sin servicios externos.
  - Se agrego `AddressForm` reutilizable.
  - Restaurantes cercanos ahora usan la direccion principal del cliente cuando existe.
  - Checkout ahora incluye pago simulado con tarjeta y validaciones locales.
  - Carrito permite vaciar carrito.
  - Se reemplazo la pagina generica del cliente por paginas dedicadas:
    - `CustomerAddressesPage`
    - `CustomerProfilePage`
    - `CustomerLoyaltyPage`
    - `CustomerComplaintsPage`
    - `CustomerReviewsPage`
  - Las rutas `/cliente/direcciones`, `/cliente/perfil`, `/cliente/fidelidad`, `/cliente/reclamos` y `/cliente/calificaciones` ahora muestran UI funcional, no JSON crudo.

- `/home/armandoaguilar/Desktop/delevery_frontend/src/api/types.ts`
  - Se agrego `LoyaltyTransaction` y `transactions` en `LoyaltyBalance` para mostrar historial de puntos.

- `/home/armandoaguilar/Desktop/delevery_frontend/src/styles.css`
  - Estilos para mini mapa, marcador, checkbox de direccion principal y mejoras visuales para formularios.

### Backend

No fue necesario agregar nuevos endpoints para completar el flujo cliente. Se reutilizaron endpoints ya existentes y funcionales:

- Auth: `/api/auth/*`
- Direcciones: `/api/users/me/addresses`
- Restaurantes: `/api/restaurants`, `/api/restaurants/search`, `/api/restaurants/nearby`
- Productos: `/api/products/restaurant/{restaurantId}`
- Carrito: `/api/cart`
- Pedidos: `/api/orders`, `/api/orders/my-history`, `/api/orders/{id}/tracking`
- Reclamos: `/api/complaints`
- Fidelidad: `/api/loyalty`
- Calificaciones: `/api/reviews`

## 4. Flujo final validado

Flujo probado por API con backend local:

1. Cliente inicia sesion con `cliente.dev@example.com`.
2. Cliente consulta direcciones.
3. Cliente consulta restaurantes cercanos con PostGIS.
4. Cliente abre menu de restaurante.
5. Cliente vacia carrito.
6. Cliente agrega producto.
7. Carrito responde:
   - subtotal: `$17.00`
   - envio estimado: `$1.75`
   - distancia: `0.38 km`
   - ETA: `24 min`
8. Cliente crea pedido desde carrito con direccion, propina y notas.
9. Pedido creado:
   - estado: `CREATED`
   - subtotal: `$17.00`
   - impuestos: `$2.21`
   - envio: `$1.75`
   - propina: `$1.25`
   - total: `$22.21`
10. Cliente consulta tracking REST del pedido.
11. Cliente crea, edita y elimina una direccion temporal con coordenadas.
12. Cliente crea reclamo sobre pedido entregado propio.
13. Cliente crea calificacion sobre pedido entregado propio.
14. Cliente consulta fidelidad:
   - puntos: `100`
   - transacciones: `5`
15. Cliente intento acceder a `GET /api/users` y recibio `403`.

## 5. Endpoints probados

| Endpoint | Resultado |
| --- | --- |
| `POST /api/auth/login` | `200` |
| `GET /api/auth/me` | `200`, rol `CUSTOMER` |
| `GET /api/users/me/addresses` | `200` |
| `POST /api/users/me/addresses` | `201`, guarda lat/lng |
| `PUT /api/users/me/addresses/{id}` | `200`, actualiza lat/lng |
| `DELETE /api/users/me/addresses/{id}` | `204` |
| `GET /api/restaurants/nearby?lat=&lng=&radiusKm=` | `200` |
| `GET /api/products/restaurant/{restaurantId}` | `200` |
| `DELETE /api/cart` | `204` |
| `POST /api/cart/items` | `200`, calcula envio/distancia/ETA |
| `POST /api/orders` | `200`, crea pedido desde carrito y pago simulado backend |
| `GET /api/orders/{id}/tracking` | `200` |
| `POST /api/complaints` | `201` |
| `POST /api/reviews` | `201` |
| `GET /api/loyalty` | `200` |
| `GET /api/users` como cliente | `403` |

## 6. Validacion tecnica

- Backend: `./mvnw test -q` exitoso.
- Frontend: `npm run lint` exitoso con warnings existentes de `react-hooks/exhaustive-deps`, sin errores.
- Frontend: `npm run build` exitoso.

## 7. Problemas pendientes

- Los warnings de `react-hooks/exhaustive-deps` siguen existiendo en varias pantallas del frontend. No bloquean build ni demo, pero conviene reducirlos en una pasada de refactor.
- El mini mapa es deliberadamente simple y no usa tiles reales. Cumple captura de coordenadas sin dependencia externa, pero no reemplaza un mapa Leaflet completo.
- El checkout muestra total estimado antes de crear pedido; el total oficial se confirma en la respuesta de `POST /api/orders` porque ahi se aplican impuestos/descuentos finales.

## 8. Conclusión

El flujo `CUSTOMER` esta listo para demostracion. Cumple con registro/login, seguridad por rol, direcciones con coordenadas PostGIS, carrito, checkout con pago simulado, pedido, tracking REST, reclamos, fidelidad y calificaciones. No se detectaron errores bloqueantes en la validacion final.
