# Guia Paso A Paso Para Desplegar Delivery

Esta guia permite preparar una instancia Ubuntu desde cero y desplegar:

- Frontend React.
- Backend Spring Boot.
- PostgreSQL con PostGIS.
- Nginx como proxy inverso.
- HTTPS gratuito con Let's Encrypt y `sslip.io`.

La aplicacion se ejecuta en una sola instancia mediante Docker Compose.

> Los comandos estan preparados para copiar y pegar. Solo reemplaza los valores
> indicados como `TU_...`.

## 1. Datos Que Debes Tener

Antes de comenzar necesitas:

- Una instancia Ubuntu en AWS EC2.
- La IP publica de la instancia.
- La llave `.pem`.
- Un token de GitHub con acceso de lectura si los repositorios son privados.
- Los puertos `22`, `80` y `443` habilitados en el Security Group.

Repositorios:

```text
https://github.com/ArmandoAgui/delivery_backend.git
https://github.com/ArmandoAgui/delivery_frontend.git
```

Ramas de despliegue:

```text
Backend:  main
Frontend: main
```

## 2. Configurar El Security Group De AWS

En AWS EC2 abre el Security Group de la instancia y agrega estas reglas de entrada:

| Tipo | Protocolo | Puerto | Origen |
| --- | --- | --- | --- |
| SSH | TCP | 22 | Tu IP publica `/32` |
| HTTP | TCP | 80 | `0.0.0.0/0` |
| HTTPS | TCP | 443 | `0.0.0.0/0` |

Si utilizas IPv6, agrega también HTTP y HTTPS desde `::/0`.

No expongas públicamente:

- `5432` de PostgreSQL.
- `8080` de Spring Boot.
- El puerto interno del frontend.

Las reglas de salida pueden permanecer abiertas.

## 3. Conectarse Por SSH

Este comando se ejecuta desde tu computadora:

```bash
chmod 400 /RUTA/A/TU_LLAVE.pem
ssh -i /RUTA/A/TU_LLAVE.pem ubuntu@TU_IP_PUBLICA
```

Ejemplo:

```bash
ssh -i /home/usuario/Downloads/Delivery.pem ubuntu@32.199.155.252
```

Los siguientes comandos se ejecutan dentro de la instancia.

## 4. Actualizar Ubuntu Y Configurar La Zona Horaria

```bash
sudo apt-get update
sudo apt-get upgrade -y
sudo timedatectl set-timezone America/El_Salvador
timedatectl
```

La salida debe mostrar:

```text
Time zone: America/El_Salvador
```

## 5. Instalar Docker Y Docker Compose

Eliminar paquetes incompatibles antiguos:

```bash
for pkg in docker.io docker-doc docker-compose podman-docker containerd runc; do
  sudo apt-get remove -y "$pkg" 2>/dev/null || true
done
```

Instalar dependencias:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg openssl
sudo install -m 0755 -d /etc/apt/keyrings
```

Agregar el repositorio oficial de Docker:

```bash
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

Instalar y habilitar Docker:

```bash
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
sudo docker version
sudo docker compose version
```

Opcionalmente, permitir que el usuario `ubuntu` ejecute Docker sin `sudo`:

```bash
sudo usermod -aG docker "$USER"
```

Este cambio se aplica en el siguiente inicio de sesión. La guia utiliza `sudo`
para que puedas continuar sin desconectarte.

## 6. Configurar El Firewall De Ubuntu

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
sudo ufw status
```

## 7. Clonar Los Repositorios

Crear la carpeta de despliegue:

```bash
mkdir -p ~/delivery-deploy
cd ~/delivery-deploy
```

### Repositorios Públicos

Si GitHub permite clonarlos sin autenticación:

```bash
git clone --branch main https://github.com/ArmandoAgui/delivery_backend.git
git clone --branch main https://github.com/ArmandoAgui/delivery_frontend.git
```

### Repositorios Privados

Este bloque solicita el token sin guardarlo en el historial ni escribirlo en el
repositorio:

```bash
read -rsp "GitHub token: " GITHUB_TOKEN
echo
export GITHUB_TOKEN

git -c credential.helper='!f() {
  if [ "$1" = get ]; then
    printf "%s\n" "username=ArmandoAgui" "password=$GITHUB_TOKEN"
  fi
}; f' clone --branch main https://github.com/ArmandoAgui/delivery_backend.git

git -c credential.helper='!f() {
  if [ "$1" = get ]; then
    printf "%s\n" "username=ArmandoAgui" "password=$GITHUB_TOKEN"
  fi
}; f' clone --branch main https://github.com/ArmandoAgui/delivery_frontend.git

unset GITHUB_TOKEN
```

Verificar:

```bash
cd ~/delivery-deploy/delivery_backend
git branch --show-current

cd ~/delivery-deploy/delivery_frontend
git branch --show-current
```

Ambos comandos deben mostrar `main`.

## 8. Crear Las Variables De Entorno

Detectar la IP publica y construir el dominio gratuito de `sslip.io`:

```bash
PUBLIC_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
DOMAIN="${PUBLIC_IP}.sslip.io"

echo "IP publica: $PUBLIC_IP"
echo "Dominio HTTPS: $DOMAIN"
```

Crear contraseñas seguras y el archivo `.env`:

```bash
cd ~/delivery-deploy/delivery_backend

DB_PASSWORD="$(openssl rand -hex 24)"
JWT_SECRET="$(openssl rand -base64 64 | tr -d '\n')"

cat > .env <<EOF
POSTGRES_DB=delivery
POSTGRES_USER=delivery
POSTGRES_PASSWORD=${DB_PASSWORD}

TZ=America/El_Salvador
HTTP_PORT=80
HTTPS_PORT=443

JWT_SECRET=${JWT_SECRET}
JWT_ACCESS_TOKEN_MINUTES=60
JWT_REFRESH_TOKEN_DAYS=14

DEV_SEED_ENABLED=true
VITE_API_BASE_URL=/api
CORS_ALLOWED_ORIGINS=https://${DOMAIN},http://${PUBLIC_IP},http://localhost

FLYWAY_BASELINE_ON_MIGRATE=false
FLYWAY_BASELINE_VERSION=0
JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -Duser.timezone=America/El_Salvador
EOF

chmod 600 .env
```

Notas:

- `DEV_SEED_ENABLED=true` crea los usuarios de demostración.
- Para un ambiente productivo real, cambia el valor a `false` después de crear
  usuarios administrativos válidos.
- No subas `.env` a GitHub.
- Guarda una copia segura de las contraseñas generadas.

Comprobar que Docker Compose puede interpretar la configuración:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env config --quiet
```

## 9. Adaptar Nginx Al Dominio De La Instancia

La configuración incluida utiliza un dominio `sslip.io`. Este bloque sustituye
el dominio anterior por el correspondiente a la IP actual:

```bash
cd ~/delivery-deploy/delivery_backend

PUBLIC_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
DOMAIN="${PUBLIC_IP}.sslip.io"
OLD_DOMAIN="$(grep -Eo '([0-9]{1,3}\.){3}[0-9]{1,3}\.sslip\.io' deploy/nginx/default.conf | head -n 1)"

test -n "$OLD_DOMAIN"
sed -i "s/${OLD_DOMAIN}/${DOMAIN}/g" deploy/nginx/default.conf

grep -n "$DOMAIN" deploy/nginx/default.conf
```

Este cambio es específico del servidor y no debe subirse desde la instancia.

## 10. Construir Base De Datos, Backend Y Frontend

Todavía no se inicia Nginx porque el certificado HTTPS no existe.

```bash
cd ~/delivery-deploy/delivery_backend

sudo docker compose -f docker-compose.aws.yml --env-file .env \
  up -d --build db backend frontend
```

Verificar:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
sudo docker compose -f docker-compose.aws.yml --env-file .env logs --tail=100 backend
```

El estado esperado es:

```text
delivery-db         healthy
delivery-backend    healthy
delivery-frontend   running
```

## 11. Arrancar Nginx Temporalmente Por HTTP

Let's Encrypt necesita consultar el puerto 80 antes de emitir el certificado.
Se utilizará una configuración temporal que no depende de archivos SSL.

```bash
cd ~/delivery-deploy/delivery_backend

PUBLIC_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
DOMAIN="${PUBLIC_IP}.sslip.io"

cat > /tmp/delivery-nginx-bootstrap.conf <<EOF
server {
    listen 80 default_server;
    server_name ${DOMAIN};

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location / {
        proxy_pass http://frontend:80;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

cat > /tmp/delivery-compose-bootstrap.yml <<EOF
services:
  nginx:
    volumes:
      - /tmp/delivery-nginx-bootstrap.conf:/etc/nginx/conf.d/default.conf:ro
EOF
```

Iniciar Nginx con esa configuración temporal:

```bash
sudo docker compose \
  -f docker-compose.aws.yml \
  -f /tmp/delivery-compose-bootstrap.yml \
  --env-file .env \
  up -d nginx
```

Probar HTTP:

```bash
curl -I "http://${DOMAIN}/"
```

Debe responder `200 OK`.

## 12. Emitir El Certificado HTTPS

Solicitar el certificado a Let's Encrypt:

```bash
cd ~/delivery-deploy/delivery_backend

PUBLIC_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
DOMAIN="${PUBLIC_IP}.sslip.io"

sudo docker compose -f docker-compose.aws.yml --env-file .env \
  run --rm --entrypoint certbot certbot \
  certonly \
  --webroot \
  -w /var/www/certbot \
  -d "$DOMAIN" \
  --register-unsafely-without-email \
  --agree-tos \
  --non-interactive
```

Verificar que se creó:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env \
  run --rm --entrypoint certbot certbot certificates
```

La salida debe incluir el dominio y una fecha de expiración.

## 13. Activar HTTPS Definitivamente

Recrear Nginx usando la configuración HTTPS del repositorio e iniciar el
servicio de renovación:

```bash
cd ~/delivery-deploy/delivery_backend

sudo docker compose -f docker-compose.aws.yml --env-file .env \
  up -d --force-recreate nginx certbot
```

Eliminar los archivos temporales:

```bash
rm -f /tmp/delivery-nginx-bootstrap.conf
rm -f /tmp/delivery-compose-bootstrap.yml
```

Verificar todos los servicios:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

Probar HTTPS:

```bash
PUBLIC_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
DOMAIN="${PUBLIC_IP}.sslip.io"

curl -I "https://${DOMAIN}/"
curl -I "https://${DOMAIN}/v3/api-docs"
```

URLs finales:

```bash
echo "Aplicacion:  https://${DOMAIN}/"
echo "Swagger:     https://${DOMAIN}/swagger-ui/index.html"
echo "OpenAPI:     https://${DOMAIN}/v3/api-docs"
```

## 14. Probar El Login

Con el seed de desarrollo activo:

```bash
PUBLIC_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
DOMAIN="${PUBLIC_IP}.sslip.io"

curl -sS -X POST "https://${DOMAIN}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin.dev@example.com","password":"Password123!"}'
```

La respuesta debe contener un `accessToken`.

Usuarios de demostración:

```text
admin.dev@example.com        / Password123!
cliente.dev@example.com      / Password123!
restaurante.dev@example.com  / Password123!
repartidor.dev@example.com   / Password123!
```

## 15. Actualizar Una Instalacion Existente

### 15.1 Obtener Los Cambios

Si no hay cambios locales en los archivos del repositorio:

```bash
cd ~/delivery-deploy/delivery_backend
git pull --ff-only origin main

cd ~/delivery-deploy/delivery_frontend
git pull --ff-only origin main
```

Si GitHub solicita autenticación para repositorios privados:

```bash
read -rsp "GitHub token: " GITHUB_TOKEN
echo
export GITHUB_TOKEN

cd ~/delivery-deploy/delivery_backend
git -c credential.helper='!f() {
  if [ "$1" = get ]; then
    printf "%s\n" "username=ArmandoAgui" "password=$GITHUB_TOKEN"
  fi
}; f' pull --ff-only origin main

cd ~/delivery-deploy/delivery_frontend
git -c credential.helper='!f() {
  if [ "$1" = get ]; then
    printf "%s\n" "username=ArmandoAgui" "password=$GITHUB_TOKEN"
  fi
}; f' pull --ff-only origin main

unset GITHUB_TOKEN
```

Si `deploy/nginx/default.conf` fue adaptado a la IP del servidor, Git puede
indicar que tiene cambios locales. Conserva una copia, restaura el archivo,
actualiza y vuelve a colocar el dominio:

```bash
cd ~/delivery-deploy/delivery_backend

PUBLIC_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
DOMAIN="${PUBLIC_IP}.sslip.io"

git restore deploy/nginx/default.conf
git pull --ff-only origin main

OLD_DOMAIN="$(grep -Eo '([0-9]{1,3}\.){3}[0-9]{1,3}\.sslip\.io' deploy/nginx/default.conf | head -n 1)"
test -n "$OLD_DOMAIN"
sed -i "s/${OLD_DOMAIN}/${DOMAIN}/g" deploy/nginx/default.conf
```

### 15.2 Reconstruir Y Aplicar

```bash
cd ~/delivery-deploy/delivery_backend

sudo docker compose -f docker-compose.aws.yml --env-file .env \
  up -d --build backend frontend

sudo docker compose -f docker-compose.aws.yml --env-file .env \
  up -d --force-recreate nginx

sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

Recrear Nginx después del backend y frontend evita que conserve direcciones
internas antiguas y produzca un `502 Bad Gateway`.

## 16. Reiniciar La Instancia

Los servicios usan `restart: unless-stopped`, por lo que Docker debe iniciarlos
automáticamente. Después de reiniciar:

```bash
cd ~/delivery-deploy/delivery_backend

sudo systemctl start docker
sudo docker compose -f docker-compose.aws.yml --env-file .env up -d
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

Si la IP publica cambió, también cambia el dominio `sslip.io`. Debes:

1. Actualizar `CORS_ALLOWED_ORIGINS` en `.env`.
2. Sustituir el dominio en `deploy/nginx/default.conf`.
3. Emitir un certificado nuevo siguiendo los pasos 11, 12 y 13.

La forma recomendada de evitar este trabajo es asignar una Elastic IP a la
instancia.

## 17. Ver Logs

```bash
cd ~/delivery-deploy/delivery_backend

sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f backend
```

En otras terminales:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f frontend
sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f nginx
sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f db
sudo docker compose -f docker-compose.aws.yml --env-file .env logs -f certbot
```

Salir de los logs con `Ctrl+C`.

## 18. Solucionar Un Error 502

Primero comprobar el estado:

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env ps
```

Revisar backend y Nginx:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env logs --tail=200 backend
sudo docker compose -f docker-compose.aws.yml --env-file .env logs --tail=200 nginx
```

Si backend está sano, recrear Nginx:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env \
  up -d --force-recreate nginx
```

Si backend no está sano:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env \
  up -d --build --force-recreate backend

sudo docker compose -f docker-compose.aws.yml --env-file .env \
  up -d --force-recreate nginx
```

## 19. Verificar Base De Datos Y PostGIS

```bash
cd ~/delivery-deploy/delivery_backend

sudo docker compose -f docker-compose.aws.yml --env-file .env exec db \
  psql -U delivery -d delivery -c "SELECT version();"

sudo docker compose -f docker-compose.aws.yml --env-file .env exec db \
  psql -U delivery -d delivery -c "SELECT postgis_version();"
```

Ver migraciones Flyway:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env exec db \
  psql -U delivery -d delivery \
  -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

## 20. Persistencia Y Copias De Seguridad

Ver volúmenes:

```bash
sudo docker volume ls
```

Volúmenes esperados:

```text
delivery_postgres_data
delivery_uploads
certbot_etc
certbot_www
```

Crear un respaldo SQL:

```bash
cd ~/delivery-deploy/delivery_backend
mkdir -p ~/delivery-backups

sudo docker compose -f docker-compose.aws.yml --env-file .env exec -T db \
  pg_dump -U delivery -d delivery \
  > ~/delivery-backups/delivery-$(date +%Y%m%d-%H%M%S).sql
```

No ejecutes este comando salvo que realmente quieras borrar la base, las
imágenes subidas y los certificados:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env down -v
```

Para detener contenedores sin borrar datos:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env down
```

## 21. Renovacion De HTTPS

El servicio `certbot` intenta renovar el certificado cada 12 horas.

Revisar sus logs:

```bash
cd ~/delivery-deploy/delivery_backend
sudo docker compose -f docker-compose.aws.yml --env-file .env logs --tail=100 certbot
```

Probar una renovación sin modificar el certificado:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env \
  run --rm --entrypoint certbot certbot renew --dry-run
```

Después de una renovación real, recargar Nginx:

```bash
sudo docker compose -f docker-compose.aws.yml --env-file .env \
  exec nginx nginx -s reload
```

## 22. Checklist Final

Ejecutar:

```bash
cd ~/delivery-deploy/delivery_backend

PUBLIC_IP="$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')"
DOMAIN="${PUBLIC_IP}.sslip.io"

sudo docker compose -f docker-compose.aws.yml --env-file .env ps

curl -I "https://${DOMAIN}/"
curl -I "https://${DOMAIN}/v3/api-docs"

curl -sS -X POST "https://${DOMAIN}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin.dev@example.com","password":"Password123!"}'
```

El despliegue está listo cuando:

- `db` aparece como `healthy`.
- `backend` aparece como `healthy`.
- `frontend`, `nginx` y `certbot` están ejecutándose.
- La página principal responde por HTTPS.
- OpenAPI responde.
- El login devuelve un token.
- El navegador no presenta errores de CORS.

