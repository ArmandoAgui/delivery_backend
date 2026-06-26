# Delivery App Deployment Handoff Guide

Este documento resume como esta desplegado el proyecto Delivery en AWS EC2 para que cualquier persona o IA pueda entenderlo, diagnosticarlo y actualizarlo sin depender del historial del chat.

## 1. Servidor

- Proveedor: AWS EC2
- IP publica actual: `32.199.155.252`
- Usuario SSH: `ubuntu`
- Llave SSH local: `/home/armandoaguilar/Downloads/Delivery.pem`
- URL publica HTTPS: `https://32.199.155.252.sslip.io/`
- URL publica HTTP: `http://32.199.155.252/` redirige a HTTPS
- Swagger UI: `https://32.199.155.252.sslip.io/swagger-ui/index.html`
- OpenAPI JSON: `https://32.199.155.252.sslip.io/v3/api-docs`
- Zona horaria del servidor: `America/El_Salvador`

Comando SSH:

```bash
ssh -i /home/armandoaguilar/Downloads/Delivery.pem ubuntu@32.199.155.252
```

Si la IP cambia, reemplazar `32.199.155.252` por la nueva IP publica.

## 2. Repositorios

Los repos estan clonados en la instancia dentro de:

```text
/home/ubuntu/delivery-deploy/
├── delivery_backend
└── delivery_frontend
```

Repos GitHub:

```text
https://github.com/ArmandoAgui/delivery_backend.git
https://github.com/ArmandoAgui/delivery_frontend.git
```

Ramas desplegadas actualmente:

```text
Backend:  feature/n-layer-architecture
Frontend: main
```

Comandos para ver estado:

```bash
cd ~/delivery-deploy/delivery_backend
git branch --show-current
git log -1 --oneline

cd ~/delivery-deploy/delivery_frontend
git branch --show-current
git log -1 --oneline
```

## 3. Arquitectura De Despliegue

La app corre en una sola instancia EC2 usando Docker Compose.

Servicios:

```text
Nginx publico
  ├── sirve / hacia frontend React
  ├── enruta /api hacia backend Spring Boot
  ├── enruta /uploads hacia backend
  └── enruta /v3/api-docs y /swagger-ui hacia backend

Frontend React
  └── imagen Nginx interna sirviendo build estatico

Backend Spring Boot
  └── API REST, JWT, Flyway, PostGIS, uploads

PostgreSQL + PostGIS
  └── base de datos persistida en volumen Docker
```

Puertos:

```text
Publico:
- 80 HTTP -> delivery-nginx, redirige a HTTPS
- 443 HTTPS -> delivery-nginx

Internos Docker:
- frontend: 80
- backend: 8080
- db: 5432
```

No se publican directamente ni PostgreSQL ni Spring Boot.

## 4. Docker Compose

El archivo principal esta en:

```bash
~/delivery-deploy/delivery_backend/docker-compose.aws.yml
```

Comando base:

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

Servicios esperados:

```text
delivery-nginx
delivery-frontend
delivery-backend
delivery-db
```

## 5. Variables De Entorno

Archivo en la instancia:

```bash
~/delivery-deploy/delivery_backend/.env
```

Permisos recomendados:

```bash
chmod 600 ~/delivery-deploy/delivery_backend/.env
```

Variables importantes:

```env
POSTGRES_DB=delivery
POSTGRES_USER=delivery
POSTGRES_PASSWORD=...

HTTP_PORT=80
HTTPS_PORT=443
VITE_API_BASE_URL=/api
CORS_ALLOWED_ORIGINS=https://32.199.155.252.sslip.io,http://32.199.155.252,http://localhost
TZ=America/El_Salvador

DEV_SEED_ENABLED=true
JWT_SECRET=...
JWT_ACCESS_TOKEN_MINUTES=60
JWT_REFRESH_TOKEN_DAYS=14

FLYWAY_BASELINE_ON_MIGRATE=false
FLYWAY_BASELINE_VERSION=0
JAVA_OPTS=-XX:MaxRAMPercentage=70.0 -Duser.timezone=America/El_Salvador
```

Si cambia la IP publica, editar:

```env
CORS_ALLOWED_ORIGINS=https://NUEVA_IP.sslip.io,http://NUEVA_IP,http://localhost
```

Luego reiniciar backend y Nginx:

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --force-recreate backend nginx
```

## 6. Volumenes Persistentes

La base de datos y uploads persisten aunque se recrean contenedores.

Volumenes:

```text
delivery_backend_delivery_postgres_data
delivery_backend_delivery_uploads
```

Ver volumenes:

```bash
sudo docker volume ls
```

No borrar estos volumenes en produccion si se quieren conservar datos.

## 7. Comandos De Actualizacion

### Actualizar backend

```bash
cd ~/delivery-deploy/delivery_backend
git pull
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --build backend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --force-recreate nginx
```

### Actualizar frontend

```bash
cd ~/delivery-deploy/delivery_frontend
git pull

cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --build frontend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --force-recreate nginx
```

### Actualizar ambos

```bash
cd ~/delivery-deploy/delivery_backend
git pull

cd ~/delivery-deploy/delivery_frontend
git pull

cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --build backend frontend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --force-recreate nginx
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

## 8. Diagnostico Rapido

### Ver estado

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

Estado sano esperado:

```text
delivery-db         healthy
delivery-backend    healthy
delivery-frontend   running
delivery-nginx      running
```

### Ver logs

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f backend
sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f frontend
sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f nginx
sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f db
```

### Probar desde la instancia

```bash
curl -I http://localhost/
curl -I -k --resolve 32.199.155.252.sslip.io:443:127.0.0.1 https://32.199.155.252.sslip.io/v3/api-docs
```

### Probar desde fuera

```bash
curl -I http://32.199.155.252/
curl -I https://32.199.155.252.sslip.io/
curl -I https://32.199.155.252.sslip.io/v3/api-docs
```

### Probar login

```bash
curl -s -X POST https://32.199.155.252.sslip.io/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin.dev@example.com","password":"Password123!"}'
```

## 9. Error 502 Bad Gateway

El 502 mas comun ocurre cuando Nginx queda apuntando a IPs internas viejas despues de reconstruir backend o frontend.

Sintoma en logs:

```text
connect() failed (111: Connection refused) while connecting to upstream
```

Solucion:

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --force-recreate nginx
```

Si aun falla:

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env restart backend frontend nginx
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

Validar:

```bash
curl -I http://localhost/
curl -I http://localhost/v3/api-docs
```

## 10. Reinicio De Instancia

Despues de reiniciar EC2:

```bash
ssh -i /home/armandoaguilar/Downloads/Delivery.pem ubuntu@NUEVA_IP

cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

Si la IP cambio:

```bash
nano ~/delivery-deploy/delivery_backend/.env
```

Actualizar:

```env
CORS_ALLOWED_ORIGINS=https://NUEVA_IP.sslip.io,http://NUEVA_IP,http://localhost
```

Luego:

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d --force-recreate backend nginx
```

## 11. Base De Datos

Contenedor:

```text
delivery-db
```

Imagen:

```text
postgis/postgis:16-3.4
```

Entrar a PostgreSQL:

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env exec db psql -U delivery -d delivery
```

Verificar PostGIS:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env exec db \
  psql -U delivery -d delivery -c "SELECT postgis_version();"
```

Ver migraciones Flyway:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env exec db \
  psql -U delivery -d delivery -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

## 12. Seed De Desarrollo

El seed esta activo con:

```env
DEV_SEED_ENABLED=true
```

Credenciales demo habituales:

```text
admin.dev@example.com        / Password123!
cliente.dev@example.com      / Password123!
restaurante.dev@example.com  / Password123!
repartidor.dev@example.com   / Password123!
```

## 13. Comandos De Limpieza Segura

Limpiar cache de build sin borrar datos:

```bash
sudo docker builder prune -af
```

Ver uso de disco:

```bash
df -h /
sudo docker system df
```

No ejecutar esto salvo que se quiera borrar la base:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env down -v
```

`down -v` elimina volumenes y borra datos persistentes.

## 14. Problemas Conocidos Y Notas

- Si se reconstruye backend/frontend, recrear Nginx para evitar 502 por upstreams viejos.
- La app usa HTTPS con `sslip.io` y certificado Let's Encrypt.
- La instancia usa una sola maquina para todo: frontend, backend, Nginx y DB.
- La base esta en volumen Docker local de la instancia, no en RDS.
- No abrir puertos de PostgreSQL ni backend al publico.
- Reglas de Security Group recomendadas:
  - Entrada TCP 80 desde `0.0.0.0/0`
  - Entrada TCP 443 desde `0.0.0.0/0`
  - Entrada TCP 22 solo desde IP confiable
  - Salida abierta por defecto

## 15. Checklist De Validacion Final

```bash
cd ~/delivery-deploy/delivery_backend

sudo docker compose -f docker-compose.aws.yml --env-file .env ps

curl -I http://localhost/
curl -I http://localhost/v3/api-docs

curl -s -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin.dev@example.com","password":"Password123!"}'
```

Desde navegador:

```text
https://32.199.155.252.sslip.io/
```

Si todo responde `200`, el despliegue esta operativo.
