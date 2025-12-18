@echo off
REM ========================================
REM Docker Development Helper Script
REM ========================================

echo.
echo ========================================
echo   AntiPanel - Docker Development
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker no esta en ejecucion.
    echo Por favor, inicia Docker Desktop y vuelve a intentarlo.
    pause
    exit /b 1
)

echo [OK] Docker esta en ejecucion
echo.

REM Ask user what to do
echo Que deseas hacer?
echo.
echo 1. Iniciar servicios (sin build)
echo 2. Iniciar servicios (con build)
echo 3. Detener servicios
echo 4. Ver logs
echo 5. Reiniciar backend
echo 6. Limpiar todo (CUIDADO: elimina datos)
echo 7. Reconstruir desde cero
echo.

set /p choice="Selecciona una opcion (1-7): "

if "%choice%"=="1" goto start
if "%choice%"=="2" goto start_build
if "%choice%"=="3" goto stop
if "%choice%"=="4" goto logs
if "%choice%"=="5" goto restart
if "%choice%"=="6" goto clean
if "%choice%"=="7" goto rebuild

echo Opcion invalida
pause
exit /b 1

:start
echo.
echo Iniciando servicios en modo desarrollo...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
echo.
echo [OK] Servicios iniciados
echo.
echo Frontend: http://localhost:4200
echo Backend:  http://localhost:8080
echo pgAdmin:  http://localhost:5050
echo.
echo Tip: Usa "docker-compose logs -f" para ver logs en tiempo real
pause
exit /b 0

:start_build
echo.
echo Iniciando servicios con rebuild...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d
echo.
echo [OK] Servicios iniciados (imagenes reconstruidas)
echo.
echo Frontend: http://localhost:4200
echo Backend:  http://localhost:8080
echo pgAdmin:  http://localhost:5050
echo.
echo Tip: Usa "docker-compose logs -f" para ver logs en tiempo real
pause
exit /b 0

:stop
echo.
echo Deteniendo servicios...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down
echo.
echo [OK] Servicios detenidos
pause
exit /b 0

:logs
echo.
echo Mostrando logs (Ctrl+C para salir)...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml logs -f
pause
exit /b 0

:restart
echo.
echo Reiniciando backend...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml restart backend
echo.
echo [OK] Backend reiniciado
pause
exit /b 0

:clean
echo.
echo [ADVERTENCIA] Esto eliminara TODOS los datos de la base de datos
set /p confirm="Estas seguro? (si/no): "
if /i not "%confirm%"=="si" (
    echo Operacion cancelada
    pause
    exit /b 0
)
echo.
echo Limpiando...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down -v
echo.
echo [OK] Todo limpio
pause
exit /b 0

:rebuild
echo.
echo Reconstruyendo desde cero...
docker-compose -f docker-compose.yml -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
pause
exit /b 0
