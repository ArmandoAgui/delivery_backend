# AWS Deployment Report

## 1. Estado Del Despliegue

Estado: parcial.

El stack Docker quedo levantado correctamente dentro de la instancia EC2 y
responde por `localhost`. El acceso publico por `http://34.224.64.79/` todavia
esta bloqueado por el Security Group de AWS, porque el puerto `80` no acepta
trafico externo.

## 2. Servicios Levantados

- Frontend: levantado en contenedor `delivery-frontend`.
- Backend: levantado en contenedor `delivery-backend`.
- PostgreSQL: levantado en contenedor `delivery-db`.
- PostGIS: activo dentro de PostgreSQL.
- Nginx: levantado en contenedor `delivery-nginx` y publicado en `0.0.0.0:80`.

## 3. URL De Acceso

- HTTP: `http://34.224.64.79/`

Nota: la URL quedara accesible desde navegador cuando el Security Group
`launch-wizard-2` permita inbound HTTP `TCP 80`.

## 4. Problemas Encontrados

- La instancia no tenia Docker ni Docker Compose instalados.
  - Se instalo `docker.io` y `docker-compose-v2`.
  - Se habilito el servicio Docker con `systemctl enable --now docker`.
- El puerto `80` responde desde dentro de la instancia, pero no desde Internet.
  - Nginx escucha correctamente en `0.0.0.0:80`.
  - `ufw` esta inactivo.
  - La instancia no tiene IAM role para modificar Security Groups desde dentro.
  - Accion pendiente en AWS Console: agregar inbound rule al Security Group
    `launch-wizard-2` para `HTTP`, `TCP 80`, origen `0.0.0.0/0` e idealmente
    tambien `::/0` si se desea IPv6.

## 5. Archivos Modificados

- `docker-compose.aws.yml`: compose productivo para EC2.
- `deploy/nginx/default.conf`: reverse proxy publico.
- `deploy/aws.env.example`: variables de entorno ejemplo para EC2.
- `README.md`: instrucciones de despliegue en EC2.
- `AWS_DEPLOYMENT_REPORT.md`: reporte operativo del despliegue.

## 6. Comandos Ejecutados

Principales comandos ejecutados:

```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2
sudo systemctl enable --now docker
sudo usermod -aG docker ubuntu
git -C delivery_backend pull --ff-only origin main
git -C delivery_frontend pull --ff-only origin main
docker compose --env-file aws.env -f docker-compose.aws.yml config
docker compose --env-file aws.env -f docker-compose.aws.yml up -d --build
docker compose --env-file aws.env -f docker-compose.aws.yml ps
docker compose --env-file aws.env -f docker-compose.aws.yml logs --tail=80 backend
docker exec delivery-db psql -U delivery -d delivery -tAc "SELECT postgis_full_version();"
curl -I http://localhost/
curl -I http://localhost/v3/api-docs
curl -I http://localhost/cliente/pedidos
curl -X POST http://localhost/api/auth/login
```

## 7. Validacion Final

Validado dentro de la instancia:

- `docker compose ps`: los cuatro contenedores estan arriba.
- Frontend local: `curl -I http://localhost/` devuelve `200 OK`.
- React Router: `curl -I http://localhost/cliente/pedidos` devuelve `200 OK`.
- OpenAPI: `curl -I http://localhost/v3/api-docs` devuelve `200 OK`.
- API protegida sin token: `GET /api/auth/me` devuelve `401`, comportamiento
  esperado.
- Login seed admin: `POST /api/auth/login` devuelve tokens y usuario.
- Restaurantes seed: `GET /api/restaurants` devuelve datos.
- Flyway: aplico 13 migraciones hasta `v13`.
- PostGIS: activo, version reportada `POSTGIS="3.4.3"`.

Pendiente externo:

- Abrir HTTP `80` en el Security Group `launch-wizard-2`. Sin esa regla, las
  pruebas desde fuera fallan con `Failed to connect to 34.224.64.79 port 80`.
