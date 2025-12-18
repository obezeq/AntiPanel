#!/bin/bash
# ========================================
# Docker Development Helper Script
# For Linux and macOS
# ========================================

echo ""
echo "========================================"
echo "  AntiPanel - Docker Development"
echo "========================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "[ERROR] Docker no está en ejecución."
    echo "Por favor, inicia Docker y vuelve a intentarlo."
    exit 1
fi

echo "[OK] Docker está en ejecución"
echo ""

# Display menu
echo "¿Qué deseas hacer?"
echo ""
echo "1. Iniciar servicios (sin build)"
echo "2. Iniciar servicios (con build)"
echo "3. Detener servicios"
echo "4. Ver logs"
echo "5. Reiniciar backend"
echo "6. Limpiar todo (CUIDADO: elimina datos)"
echo "7. Reconstruir desde cero"
echo ""

read -p "Selecciona una opción (1-7): " choice

case $choice in
    1)
        echo ""
        echo "Iniciando servicios en modo desarrollo..."
        docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
        echo ""
        echo "[OK] Servicios iniciados"
        echo ""
        echo "Frontend: http://localhost:4200"
        echo "Backend:  http://localhost:8080"
        echo "pgAdmin:  http://localhost:5050"
        echo ""
        echo "Tip: Usa 'docker compose logs -f' para ver logs en tiempo real"
        ;;
    2)
        echo ""
        echo "Iniciando servicios con rebuild..."
        docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d
        echo ""
        echo "[OK] Servicios iniciados (imágenes reconstruidas)"
        echo ""
        echo "Frontend: http://localhost:4200"
        echo "Backend:  http://localhost:8080"
        echo "pgAdmin:  http://localhost:5050"
        echo ""
        echo "Tip: Usa 'docker compose logs -f' para ver logs en tiempo real"
        ;;
    3)
        echo ""
        echo "Deteniendo servicios..."
        docker compose -f docker-compose.yml -f docker-compose.dev.yml down
        echo ""
        echo "[OK] Servicios detenidos"
        ;;
    4)
        echo ""
        echo "Mostrando logs (Ctrl+C para salir)..."
        docker compose -f docker-compose.yml -f docker-compose.dev.yml logs -f
        ;;
    5)
        echo ""
        echo "Reiniciando backend..."
        docker compose -f docker-compose.yml -f docker-compose.dev.yml restart backend
        echo ""
        echo "[OK] Backend reiniciado"
        ;;
    6)
        echo ""
        echo "[ADVERTENCIA] Esto eliminará TODOS los datos de la base de datos"
        read -p "¿Estás seguro? (si/no): " confirm
        if [ "$confirm" != "si" ]; then
            echo "Operación cancelada"
            exit 0
        fi
        echo ""
        echo "Limpiando..."
        docker compose -f docker-compose.yml -f docker-compose.dev.yml down -v
        echo ""
        echo "[OK] Todo limpio"
        ;;
    7)
        echo ""
        echo "Reconstruyendo desde cero..."
        docker compose -f docker-compose.yml -f docker-compose.dev.yml down -v
        docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
        ;;
    *)
        echo "Opción inválida"
        exit 1
        ;;
esac
