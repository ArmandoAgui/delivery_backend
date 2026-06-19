# Modulo de Restaurantes

Este documento resume los endpoints disponibles para el modulo de restaurantes y las reglas de negocio aplicadas en la rama `feature/crud-restaurant`.

## Base URL

```text
/restaurants
```

## Endpoints

### Crear restaurante

```http
POST /restaurants
```

Crea un restaurante asociado a un usuario dueno con rol `RESTAURANT`.

Request:

```json
{
  "ownerId": "018f1000-0000-7000-8000-000000000003",
  "name": "Pupuseria Demo",
  "description": "Restaurante de prueba para delivery",
  "phone": "+50370001000",
  "email": "pupuseria.demo@example.com",
  "streetAddress": "Boulevard de Los Proceres 500",
  "city": "San Salvador",
  "state": "San Salvador",
  "country": "El Salvador",
  "latitude": 13.6929,
  "longitude": -89.232
}
```

Respuestas principales:

- `201 Created`: restaurante creado.
- `400 Bad Request`: datos invalidos.
- `404 Not Found`: el dueno no existe, no esta activo o no tiene rol `RESTAURANT`.
- `409 Conflict`: el dueno ya tiene un restaurante.

### Listar restaurantes activos

```http
GET /restaurants
```

Retorna los restaurantes activos. No retorna restaurantes desactivados por soft delete.

El campo `open` se calcula dinamicamente con base en el horario del dia actual.

### Obtener restaurante por id

```http
GET /restaurants/{id}
```

Retorna un restaurante activo por id.

Respuestas principales:

- `200 OK`: restaurante encontrado.
- `404 Not Found`: restaurante inexistente o inactivo.

### Actualizar restaurante

```http
PUT /restaurants/{id}
```

Actualiza los datos principales del restaurante. No cambia el estado `active`; la baja se maneja con soft delete.

Request:

```json
{
  "name": "Pupuseria Demo Actualizada",
  "description": "Restaurante actualizado",
  "phone": "+50370001001",
  "email": "pupuseria.actualizada@example.com",
  "streetAddress": "Boulevard de Los Proceres 501",
  "city": "San Salvador",
  "state": "San Salvador",
  "country": "El Salvador",
  "latitude": 13.693,
  "longitude": -89.231
}
```

Respuestas principales:

- `200 OK`: restaurante actualizado.
- `400 Bad Request`: datos invalidos.
- `404 Not Found`: restaurante inexistente o inactivo.

### Desactivar restaurante

```http
PATCH /restaurants/{id}/deactivate
```

Aplica soft delete. No elimina el registro fisicamente; solamente marca `is_active = false`.

Respuestas principales:

- `204 No Content`: restaurante desactivado.
- `404 Not Found`: restaurante inexistente o ya inactivo.

Nota: no existe endpoint `DELETE /restaurants/{id}` para este modulo.

### Listar restaurantes abiertos

```http
GET /restaurants/open
```

Retorna solo restaurantes activos que estan abiertos en este momento segun su horario de servicio.

Regla aplicada:

- Si no hay horario para el dia actual, el restaurante se considera cerrado.
- Si el horario del dia esta marcado como cerrado, el restaurante se considera cerrado.
- Si la hora actual esta entre `opensAt` y `closesAt`, el restaurante se considera abierto.

### Listar horarios de un restaurante

```http
GET /restaurants/{id}/schedules
```

Retorna los horarios configurados del restaurante, ordenados por dia de la semana.

Ejemplo de respuesta:

```json
[
  {
    "id": 3,
    "dayOfWeek": 7,
    "opensAt": "00:00:00",
    "closesAt": "23:59:59",
    "closed": false
  }
]
```

### Crear o actualizar horarios

```http
PUT /restaurants/{id}/schedules
```

Crea o actualiza los horarios del restaurante. Si ya existe un horario para el dia enviado, lo actualiza.

Request para dia abierto:

```json
[
  {
    "dayOfWeek": 7,
    "opensAt": "09:00:00",
    "closesAt": "21:00:00",
    "closed": false
  }
]
```

Request para dia cerrado:

```json
[
  {
    "dayOfWeek": 7,
    "closed": true
  }
]
```

Respuestas principales:

- `200 OK`: horarios actualizados.
- `400 Bad Request`: horarios invalidos.
- `404 Not Found`: restaurante inexistente o inactivo.

## Reglas de negocio aplicadas

- Solo usuarios activos con rol `RESTAURANT` pueden ser duenos de restaurante.
- Un mismo dueno solo puede tener un restaurante. Si intenta crear otro, se responde `409 Conflict` con el mensaje `Restaurant owner already has a restaurant`.
- El modulo no usa borrado fisico. La baja del restaurante se maneja con soft delete usando `PATCH /restaurants/{id}/deactivate`.
- Las consultas generales solo muestran restaurantes activos.
- El estado `open` no se controla directamente desde el request; se calcula con los horarios configurados.
- Los horarios usan `dayOfWeek` del `1` al `7`, siguiendo `java.time.DayOfWeek`: lunes `1`, domingo `7`.
- Para un dia abierto, `opensAt` y `closesAt` son obligatorios.
- `closesAt` debe ser posterior a `opensAt`.
- No se permiten dias duplicados en el mismo request de horarios.
- Para un dia cerrado, `opensAt` y `closesAt` se guardan como `null`.

## Nota tecnica sobre horarios

Los campos `opensAt` y `closesAt` se mapearon como `LOCAL_TIME` para respetar la columna `TIME WITHOUT TIME ZONE` de PostgreSQL. Esto evita que Hibernate aplique desplazamientos por zona horaria al guardar horas de servicio.

## Pruebas realizadas

Se probaron los endpoints de horarios contra la app local:

- Sin horarios configurados, el restaurante responde `open: false`.
- Un horario abierto sin `opensAt` y `closesAt` responde `400 Bad Request`.
- Un horario valido de domingo `00:00:00` a `23:59:59` responde `200 OK` y el restaurante queda `open: true`.
- Un domingo marcado como `closed: true` deja el restaurante `open: false`.
- Un horario fuera de la hora actual deja el restaurante `open: false`.

Tambien se ejecuto:

```bash
./mvnw test
```

Resultado: `30 tests`, todos exitosos.
