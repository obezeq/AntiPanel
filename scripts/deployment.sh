#!/bin/bash
set -euo pipefail

echo "-------------------------------------"
echo "   AntiPanel Deployment Automation   "
echo "-------------------------------------"
echo ""

# Definimos la ruta base del proyecto una sola vez
APP_DIR="$HOME/AntiPanel"

# Nos aseguramos de que el directorio del proyecto existe antes de intentar entrar
if [ ! -d "$APP_DIR" ]; then
    echo "ERROR: El directorio del proyecto no existe en $APP_DIR"
    exit 1
fi

echo "[INFO] Cambiando al directorio del proyecto: $APP_DIR"
cd "$APP_DIR"

echo "[1/6] Verificando Docker y Docker Compose..."
# Instalar Docker si no está presente
if ! command -v docker &> /dev/null; then
    echo "[!] Docker no encontrado. Instalando..."
    curl -fsSL https://get.docker.com | sh
    sudo usermod -aG docker $USER
    echo "[+] Docker instalado"
else
    echo "[+] Docker ya instalado: $(docker --version)"
fi

# Verificar Docker Compose v2 (plugin)
if ! docker compose version &> /dev/null; then
    echo "[!] Docker Compose v2 no encontrado. Instalando plugin..."
    sudo apt-get update
    sudo apt-get install -y docker-compose-plugin
    echo "[+] Docker Compose v2 instalado"
else
    echo "[+] Docker Compose ya instalado: $(docker compose version)"
fi

echo "[2/6] Actualizando solo el código rastreado por Git (sin tocar .env.prod)..."
git fetch origin
git reset --hard origin/main
echo "[+] Código actualizado"

echo "[3/6] Verificando .env.prod..."
# Usamos rutas absolutas para las comprobaciones, haciéndolas independientes del directorio actual
[ -f "$APP_DIR/.env.prod" ]     || { echo "ERROR: Falta el archivo .env.prod en $APP_DIR"; exit 1; }
echo "[+] .env.prod presente"

echo "[4/6] Deteniendo contenedores..."
# Usamos rutas absolutas para los archivos de Docker Compose
docker compose -f "$APP_DIR/docker-compose.yml" -f "$APP_DIR/docker-compose.prod.yml" --env-file "$APP_DIR/.env.prod" down || true

echo "[5/6] Reconstruyendo y levantando..."
# Sin --no-cache para aprovechar Docker layer caching (reduce ~8min a ~2-3min)
docker compose -f "$APP_DIR/docker-compose.yml" -f "$APP_DIR/docker-compose.prod.yml" --env-file "$APP_DIR/.env.prod" build
docker compose -f "$APP_DIR/docker-compose.yml" -f "$APP_DIR/docker-compose.prod.yml" --env-file "$APP_DIR/.env.prod" up -d --force-recreate

echo "[6/6] Limpiando imágenes antiguas..."
docker image prune -f --filter "dangling=true" >/dev/null 2>&1 || true

echo ""
echo "======================================="
echo "  AntiPanel desplegada correctamente :D  "
echo "======================================="