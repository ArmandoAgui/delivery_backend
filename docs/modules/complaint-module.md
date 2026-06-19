# Complaint Module

## Objetivo

El modulo `complaint` administra reclamos de clientes relacionados con pedidos entregados. Sigue el flujo del monolito modular:

```text
controller -> service -> repository -> database
entity -> mapper -> dto -> response
```

## Tablas Afectadas

### complaints

Tabla principal del modulo.

Uso:

- Crea un reclamo asociado a un pedido.
- Guarda cliente, asunto, descripcion, estado y resolucion.
- Evita reclamos duplicados por pedido mediante `uk_complaints_order_id`.
- Cambia el flujo de estados a:
  - `OPEN`
  - `IN_PROGRESS`
  - `RESOLVED`
  - `REJECTED`

Cambios por migracion `V5__align_complaint_statuses_and_refunds.sql`:

- Convierte estados anteriores:
  - `IN_REVIEW` -> `IN_PROGRESS`
  - `CLOSED` -> `RESOLVED`
- Reemplaza el constraint `chk_complaints_status`.
- Asegura constraint unico por `order_id` si no existe.

### refunds

Tabla usada para el reembolso academico simple.

Uso:

- Al resolver un reclamo (`IN_PROGRESS -> RESOLVED`), se crea un refund aprobado.
- El refund queda asociado al reclamo y al pago pagado del pedido.
- El estado usado por el modulo es `APPROVED`.

Cambios por migracion:

- Asegura el indice `idx_refunds_status` si no existe.

### orders

Tabla consultada para validar reglas de negocio.

Uso:

- Verificar que el pedido exista.
- Verificar que el pedido este en estado `DELIVERED`.
- Verificar que el usuario autenticado sea el cliente propietario del pedido.

No se modifica directamente desde el modulo `complaint`.

### users

Tabla consultada para validar autenticacion logica y roles.

Uso:

- Verificar que el cliente exista y este activo.
- Verificar que el usuario que cambia estados sea `ADMIN`.
- Verificar permisos de consulta del reclamo.

No se modifica directamente desde el modulo `complaint`.

### payments

Tabla consultada para ejecutar el reembolso simple.

Uso:

- Buscar el ultimo pago `PAID` del pedido.
- Tomar el monto del pago para crear el refund.

No se modifica directamente desde el modulo `complaint`.

## Endpoints

### Crear reclamo

```http
POST /api/complaints
```

Body:

```json
{
  "orderId": "uuid",
  "subject": "Producto faltante",
  "description": "Falto una bebida en el pedido entregado."
}
```

Respuesta esperada:

- `201 Created`
- Reclamo en estado `OPEN`.

Validaciones:

- El usuario debe existir y tener rol `CUSTOMER`.
- El pedido debe existir.
- El pedido debe estar `DELIVERED`.
- El pedido debe pertenecer al cliente autenticado.
- No debe existir otro reclamo para el mismo pedido.

### Listar reclamos

```http
GET /api/complaints
```

Filtros opcionales:

```http
GET /api/complaints?status=RESOLVED&orderId=uuid
```

Comportamiento:

- Si el usuario es `ADMIN`, puede listar todos.
- Si no es admin, solo ve sus propios reclamos.

### Detalle de reclamo

```http
GET /api/complaints/{id}
```

Comportamiento:

- Devuelve detalle del reclamo.
- Incluye refund si existe.
- Retorna `404` si no existe.

### Cambiar estado

```http
PATCH /api/complaints/{id}/status
```

Body:

```json
{
  "status": "IN_PROGRESS"
}
```

Solo usuarios `ADMIN` activos pueden cambiar estados.

Transiciones validas:

```text
OPEN -> IN_PROGRESS
IN_PROGRESS -> RESOLVED
IN_PROGRESS -> REJECTED
```

Transiciones bloqueadas:

```text
RESOLVED -> OPEN
REJECTED -> OPEN
RESOLVED -> IN_PROGRESS
```

Tambien se bloquea cualquier modificacion si el reclamo ya esta `RESOLVED` o `REJECTED`.

## Reembolso Simple

El reembolso simple se ejecuta automaticamente cuando el reclamo pasa de:

```text
IN_PROGRESS -> RESOLVED
```

Reglas:

- Debe existir un pago `PAID` para el pedido.
- No debe existir ya un refund para el reclamo.
- Se crea un registro en `refunds`.
- El estado queda `APPROVED`.
- El monto se toma del pago.

## Archivos Principales

- `src/main/java/sv/edu/uca/delivery/backend/complaint/controller/ComplaintController.java`
- `src/main/java/sv/edu/uca/delivery/backend/complaint/service/ComplaintService.java`
- `src/main/java/sv/edu/uca/delivery/backend/complaint/entity/Complaint.java`
- `src/main/java/sv/edu/uca/delivery/backend/complaint/entity/Refund.java`
- `src/main/java/sv/edu/uca/delivery/backend/complaint/repository/ComplaintRepository.java`
- `src/main/java/sv/edu/uca/delivery/backend/complaint/repository/RefundRepository.java`
- `src/main/java/sv/edu/uca/delivery/backend/complaint/mapper/ComplaintMapper.java`
- `src/main/resources/db/migration/V5__align_complaint_statuses_and_refunds.sql`

## Pruebas Realizadas

Pruebas automatizadas:

```text
./mvnw test
Tests run: 12, Failures: 0, Errors: 0
```

Pruebas manuales contra `localhost:8080`:

- `GET /api/complaints` devuelve `200`.
- `POST /api/complaints` crea reclamo `OPEN`.
- Duplicar reclamo por pedido devuelve `400`.
- Crear reclamo sobre pedido no entregado devuelve `400`.
- `GET /api/complaints/{id}` devuelve detalle.
- `PATCH OPEN -> IN_PROGRESS` devuelve `200`.
- `PATCH IN_PROGRESS -> RESOLVED` devuelve `200` y crea refund `APPROVED`.
- Intentar modificar un reclamo `RESOLVED` devuelve `400`.

## Datos de Prueba Usados

Durante la prueba manual se usaron datos existentes de Supabase y se agregaron datos minimos para validar el flujo completo:

- Pedido entregado: `018f0000-0000-7000-8000-000000000401`
- Cliente: `018f0000-0000-7000-8000-000000000001`
- Admin demo: `018f0000-0000-7000-8000-000000000007`
- Pago demo `PAID` para probar refund.

## Notas Tecnicas

- Los IDs usan UUID v7 generado en backend mediante `UuidV7Generator`.
- No se modifico la configuracion global de Supabase/PostgreSQL.
- Flyway sigue usando `classpath:db/migration`.
- La migracion de complaint se agrego como `V5` para no sobrescribir `V4`, que pertenece al modulo delivery.
