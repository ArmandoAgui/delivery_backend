# Functional Completion Report

Fecha: 2026-06-20

## 1. Funcionalidades Implementadas

### Calificaciones

- Se mantuvo la regla existente de negocio para crear calificaciones:
  - Solo el cliente propietario del pedido puede calificar.
  - Solo pedidos `DELIVERED`.
  - Una calificacion por pedido y usuario.
  - Rating validado entre 1 y 5.
  - Comentario opcional.
  - No permite calificar pedidos cancelados porque el servicio exige `DELIVERED`.
- Se agrego calculo de promedio y cantidad de calificaciones por restaurante.
- Se agrego calculo de promedio y cantidad de calificaciones por repartidor.
- Se agrego endpoint para consultar reviews de un repartidor:
  - `GET /api/reviews/delivery/{deliveryUserId}`
- Los endpoints de restaurante ahora devuelven:
  - `averageRating`
  - `reviewCount`
- El perfil de repartidor ahora devuelve:
  - `averageRating`
  - `reviewCount`

### Frontend De Calificaciones

- En el catalogo y detalle de restaurante se muestra promedio de calificacion.
- En el perfil del restaurante se muestra promedio y cantidad de reviews.
- En el perfil del repartidor se muestra promedio y cantidad de reviews.
- El modal de pedidos entregados ahora comunica que la calificacion aplica a restaurante y repartidor del pedido.

### Horario Pico

- Se agrego banner visible para restaurante durante horarios pico:
  - 11:00 a 13:00
  - 17:00 a 19:00
- El banner aparece en:
  - Dashboard del restaurante.
  - Perfil del restaurante.
  - Horarios.
  - Pedidos recibidos.
- No usa WebSockets, correo ni push notifications; se evalua al cargar la pantalla.

## 2. Escalabilidad De Creacion De Pedidos

Se reviso el flujo actual y se decidio no reemplazarlo por un Builder nuevo.

Motivo:

- `OrderService.createFromCart` contiene la orquestacion de negocio.
- `OrderFactory.fromCart` centraliza la construccion del `Order`.
- El factory ya evita constructores extensos y setters dispersos fuera del flujo controlado.
- El flujo soporta atributos opcionales y extensibles:
  - subtotal
  - impuestos
  - costo de envio
  - ETA
  - distancia
  - horario pico
  - propina
  - cupon
  - fidelidad
  - notas

Conclusion:

La solucion actual cumple adecuadamente el objetivo academico del desafio de escalabilidad. Un Builder fluido seria posible, pero en este momento aportaria mas cambio cosmetico que beneficio real.

## 3. Como Probar

### Backend

```bash
cd /home/armandoaguilar/Desktop/delevery_backend
./mvnw -q test
```

Resultado validado:

- Tests exitosos.
- 47 tests ejecutados.
- 0 fallos.

### Frontend

```bash
cd /home/armandoaguilar/Desktop/delevery_frontend
npm run build
```

Resultado validado:

- TypeScript compila.
- Vite genera build productivo.

### Flujo Manual De Calificaciones

1. Iniciar backend y frontend.
2. Login como cliente.
3. Crear pedido desde carrito.
4. Login como restaurante.
5. Confirmar pedido.
6. Login como repartidor.
7. Aceptar solicitud y avanzar a `DELIVERED`.
8. Volver como cliente.
9. Ir a pedidos realizados.
10. Abrir modal `Calificar`.
11. Enviar rating de 1 a 5 y comentario opcional.
12. Verificar que el restaurante muestra promedio actualizado.
13. Login como repartidor y verificar promedio en perfil.

### Flujo Manual De Horario Pico

1. Entrar como restaurante entre 11:00-13:00 o 17:00-19:00.
2. Abrir dashboard, perfil, horarios o pedidos.
3. Verificar banner:
   - `Horario pico activo. Revisa constantemente los pedidos y mantén actualizado el tiempo de preparación.`

## 4. Archivos Modificados

Backend:

- `src/main/java/sv/edu/uca/delivery/backend/review/repository/ReviewRepository.java`
- `src/main/java/sv/edu/uca/delivery/backend/review/controller/ReviewController.java`
- `src/main/java/sv/edu/uca/delivery/backend/review/service/ReviewService.java`
- `src/main/java/sv/edu/uca/delivery/backend/restaurant/dto/response/RestaurantResponseDTO.java`
- `src/main/java/sv/edu/uca/delivery/backend/restaurant/mapper/RestaurantMapper.java`
- `src/main/java/sv/edu/uca/delivery/backend/restaurant/service/impl/RestaurantServiceImpl.java`
- `src/main/java/sv/edu/uca/delivery/backend/delivery/dto/DeliveryProfileResponse.java`
- `src/main/java/sv/edu/uca/delivery/backend/delivery/service/DeliveryService.java`

Frontend:

- `/home/armandoaguilar/Desktop/delevery_frontend/src/api/types.ts`
- `/home/armandoaguilar/Desktop/delevery_frontend/src/App.tsx`
- `/home/armandoaguilar/Desktop/delevery_frontend/src/styles.css`

Documentacion:

- `FUNCTIONAL_COMPLETION_REPORT.md`

## 5. Decisiones Tecnicas

- No se agregaron columnas nuevas porque la tabla `reviews` ya almacena pedido, restaurante, usuario, repartidor opcional, rating, comentario y fecha.
- El promedio se calcula por consulta para evitar desnormalizacion prematura.
- La calificacion se mantiene como una review por pedido, asociada a restaurante y repartidor si el pedido tuvo delivery asignado.
- El horario pico se calcula en frontend para notificacion visual, mientras el backend mantiene su propio calculo para costo/ETA.
