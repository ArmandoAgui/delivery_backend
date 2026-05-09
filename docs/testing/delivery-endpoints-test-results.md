# Delivery Endpoints Test Results

Fecha de ejecucion: 2026-05-08

Comando ejecutado:

```bash
./mvnw test
```

Resultado general:

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Alcance

Los endpoints del modulo delivery se probaron con `MockMvc` usando un `DeliveryService` mockeado. Esto valida el contrato HTTP del controller, serializacion JSON, validaciones de request y codigos de respuesta sin depender de Supabase, Flyway ni autenticacion real.

Archivo de pruebas:

```text
src/test/java/sv/edu/uca/delivery/backend/delivery/controller/DeliveryControllerTest.java
```

## Endpoints Probados

| Caso | Endpoint | Entrada | Resultado esperado | Estado |
| --- | --- | --- | --- | --- |
| Asignar repartidor | `POST /api/deliveries/assign` | `{"orderId":"<uuid>"}` | `201 Created` con `DeliveryResponse` | OK |
| Asignar sin `orderId` | `POST /api/deliveries/assign` | `{}` | `400 Bad Request` con error de validacion | OK |
| Obtener mis pedidos | `GET /api/deliveries/my-orders` | N/A | `200 OK` con lista de pedidos asignados | OK |
| Actualizar estado | `PATCH /api/deliveries/{id}/status` | `{"status":"ON_THE_WAY"}` | `200 OK` con `DeliveryResponse` actualizado | OK |
| Actualizar sin `status` | `PATCH /api/deliveries/{id}/status` | `{}` | `400 Bad Request` con error de validacion | OK |

## Detalle Por Test

### `assignDeliveryReturnsCreatedResponse`

Valida que `POST /api/deliveries/assign`:

- acepte un `orderId` valido;
- retorne HTTP `201 Created`;
- responda con `id`, `orderId`, `deliveryUserId` y `status`;
- invoque `DeliveryService.assignDelivery(...)`.

Resultado: OK.

### `assignDeliveryReturnsBadRequestWhenOrderIdIsMissing`

Valida que `POST /api/deliveries/assign`:

- rechace un body sin `orderId`;
- retorne HTTP `400 Bad Request`;
- use el handler global con mensaje `Invalid request payload`;
- no invoque el service.

Resultado: OK.

### `getMyOrdersReturnsAssignedOrders`

Valida que `GET /api/deliveries/my-orders`:

- retorne HTTP `200 OK`;
- responda una lista JSON;
- incluya los campos principales del delivery asignado;
- invoque `DeliveryService.getMyOrders()`.

Resultado: OK.

### `updateStatusReturnsUpdatedDelivery`

Valida que `PATCH /api/deliveries/{id}/status`:

- acepte un UUID en path;
- acepte un status valido;
- retorne HTTP `200 OK`;
- responda el assignment actualizado;
- invoque `DeliveryService.updateStatus(...)`.

Resultado: OK.

### `updateStatusReturnsBadRequestWhenStatusIsMissing`

Valida que `PATCH /api/deliveries/{id}/status`:

- rechace un body sin `status`;
- retorne HTTP `400 Bad Request`;
- use el handler global con mensaje `Invalid request payload`;
- no invoque el service.

Resultado: OK.

## Resultado De La Suite Completa

Ademas de los tests de endpoints, se ejecutaron:

- tests de reglas de negocio del service;
- tests del generador UUID v7;
- smoke test basico de clase principal.

Salida final:

```text
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Nota

Estos tests no hacen llamadas HTTP reales contra `localhost:8080`; son tests automatizados de controller con `MockMvc`. Para pruebas manuales contra la base de Supabase, cargar primero:

```text
src/main/resources/db/seed/delivery_test_data.sql
```

y luego ejecutar el proyecto con las variables de `.env`.
