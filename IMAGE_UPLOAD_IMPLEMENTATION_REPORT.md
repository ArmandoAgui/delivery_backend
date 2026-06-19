# Image Upload Implementation Report

## 1. Cambios Realizados En Backend

- Se agrego almacenamiento local configurable para imagenes en `app.uploads`.
- Se agrego servicio comun `ImageStorageService` para validar, redimensionar,
  convertir/comprimir y guardar imagenes.
- Se agrego exposicion estatica de archivos bajo `/uploads/**`.
- Se agregaron endpoints multipart para restaurantes:
  - `POST /restaurants/{id}/image`
  - `POST /api/restaurants/{id}/image`
  - `DELETE /restaurants/{id}/image`
  - `DELETE /api/restaurants/{id}/image`
- Se agregaron endpoints multipart para productos:
  - `POST /products/{id}/image`
  - `POST /api/products/{id}/image`
  - `DELETE /products/{id}/image`
  - `DELETE /api/products/{id}/image`
- Se expuso `imageUrl` en `RestaurantResponseDTO` y `ProductResponseDTO`.
- Se agrego migracion `V14__restaurant_and_product_images.sql`.
- Se configuro volumen Docker `delivery_uploads` para persistir archivos.
- Se agrego proxy Nginx para `/uploads/`.

## 2. Cambios Realizados En Frontend

- Se agrego helper `uploadFile` para `multipart/form-data`.
- Se agrego `imageUrl` a los tipos `Restaurant` y `Product`.
- Se agrego componente reutilizable `ImageUploader`.
- En perfil de restaurante:
  - vista previa de imagen;
  - subida/reemplazo;
  - eliminacion.
- En productos del restaurante:
  - vista previa por producto;
  - subida/reemplazo;
  - eliminacion.
- En cliente:
  - listado de restaurantes muestra imagen o placeholder;
  - detalle de restaurante muestra imagen;
  - tarjetas de productos muestran imagen o placeholder.
- Se agrego proxy `/uploads` en Vite y Nginx del frontend.

## 3. Estrategia De Compresion

- Se valida el contenido real usando `ImageIO.read`.
- Se redimensiona manteniendo proporcion si la imagen supera el ancho maximo.
- Restaurante: ancho maximo `1280px`.
- Producto: ancho maximo `800px`.
- Se convierte a RGB para eliminar canal alfa y metadatos no necesarios.
- Se intenta escribir WebP con calidad configurable.
- Si no existiera writer WebP en runtime, el servicio usa fallback JPEG.

Validacion realizada:

- `ImageIO.getImageWritersByFormatName("webp").hasNext()` devolvio `true`.

## 4. Formato Final Almacenado

- Preferente: `.webp`.
- Fallback tecnico: `.jpg`.
- La ruta relativa queda guardada en base de datos, por ejemplo:

```text
/uploads/restaurants/restaurant-<uuid>.webp
/uploads/products/product-<uuid>.webp
```

## 5. Ubicacion De Archivos En Disco

Por defecto:

```text
uploads/
├── restaurants/
└── products/
```

En Docker:

```text
/app/uploads
```

Volumen Docker:

```text
delivery_uploads
```

Variables:

```text
UPLOADS_ROOT_PATH=/app/uploads
UPLOADS_PUBLIC_PATH=/uploads
UPLOADS_MAX_FILE_SIZE_BYTES=10485760
UPLOADS_RESTAURANT_MAX_WIDTH=1280
UPLOADS_PRODUCT_MAX_WIDTH=800
UPLOADS_IMAGE_QUALITY=0.82
```

## 6. Validaciones Implementadas

- Archivo requerido.
- Tamano maximo antes de compresion: `10 MB`.
- Content types permitidos:
  - `image/jpeg`
  - `image/png`
  - `image/webp`
- Extensiones permitidas:
  - `jpg`
  - `jpeg`
  - `png`
  - `webp`
- Validacion de contenido real con `ImageIO`.
- Prevencion de path traversal al resolver rutas.
- Reemplazo elimina archivo anterior si cambia la ruta final.
- Eliminacion de restaurante/producto tambien limpia su imagen.
- Solo propietario de restaurante o ADMIN puede modificar imagenes.
- Clientes y repartidores no pueden subir ni eliminar imagenes.

## 7. Como Probar Manualmente

### Restaurante

1. Iniciar sesion con:

```text
restaurante.dev@example.com
Password123!
```

2. Ir a `Restaurante > Perfil`.
3. Si no existe restaurante, crearlo primero.
4. En `Imagen del restaurante`, elegir una imagen JPG/PNG/WEBP.
5. Confirmar que aparece vista previa.
6. Guardar/reemplazar otra imagen.
7. Confirmar que la imagen anterior se reemplaza.
8. Eliminar imagen.
9. Confirmar que desaparece la vista previa.

### Producto

1. Iniciar sesion como restaurante.
2. Ir a `Restaurante > Productos`.
3. Crear categoria si hace falta.
4. Crear producto.
5. En la fila del producto, elegir imagen.
6. Confirmar vista previa.
7. Cambiar imagen.
8. Eliminar imagen.

### Cliente

1. Iniciar sesion con:

```text
cliente.dev@example.com
Password123!
```

2. Ir a `Cliente > Restaurantes`.
3. Confirmar que la imagen del restaurante aparece en la card.
4. Entrar al restaurante.
5. Confirmar que productos muestran imagen.

### Validaciones Negativas

- Subir archivo `.pdf` o `.txt`: debe responder `400`.
- Subir archivo mayor a `10 MB`: debe responder `400`.
- Intentar subir imagen como cliente: debe responder `403`.
- Intentar subir imagen a producto de otro restaurante: debe responder `403`.

## 8. Evidencia De Validacion

- Backend: `./mvnw test -q` exitoso.
- Frontend: `npm run build` exitoso.
- WebP writer disponible: `true`.

## 9. Consideraciones Para EC2

- El volumen `delivery_uploads` debe conservarse entre reinicios.
- No borrar el volumen si se desea preservar imagenes.
- Nginx publica `/uploads/` hacia el backend.
- Si se migra a otra instancia, copiar el contenido del volumen o del directorio
  configurado en `UPLOADS_ROOT_PATH`.
