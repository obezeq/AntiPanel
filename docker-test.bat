@echo off
REM ========================================
REM AntiPanel - Docker Test Helper (Windows)
REM Helper script para ejecutar tests en Docker
REM ========================================

title AntiPanel - Docker Test Helper

:menu
cls
echo ========================================
echo   AntiPanel - Docker Test Helper
echo ========================================
echo.
echo   [1] Ejecutar TODOS los tests
echo   [2] Ejecutar tests con reportes detallados
echo   [3] Ejecutar tests de repositorios solamente
echo   [4] Ver reportes HTML de tests
echo   [5] Limpiar reportes antiguos
echo   [6] Ver logs de tests
echo   [7] Ejecutar tests en modo continuo (watch)
echo.
echo   [0] Salir
echo.
echo ========================================
set /p choice="Selecciona una opcion: "

if "%choice%"=="1" goto run_all_tests
if "%choice%"=="2" goto run_tests_detailed
if "%choice%"=="3" goto run_repository_tests
if "%choice%"=="4" goto view_reports
if "%choice%"=="5" goto clean_reports
if "%choice%"=="6" goto view_logs
if "%choice%"=="7" goto watch_mode
if "%choice%"=="0" goto end
goto menu

:run_all_tests
cls
echo ========================================
echo   Ejecutando TODOS los tests...
echo ========================================
echo.
docker-compose -f docker-compose.yml -f docker-compose.test.yml up --build --abort-on-container-exit
echo.
echo Presiona cualquier tecla para volver al menu...
pause >nul
goto menu

:run_tests_detailed
cls
echo ========================================
echo   Ejecutando tests con output detallado...
echo ========================================
echo.
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --info"
echo.
echo Presiona cualquier tecla para volver al menu...
pause >nul
goto menu

:run_repository_tests
cls
echo ========================================
echo   Ejecutando tests de repositorios...
echo ========================================
echo.
echo Ingresa el nombre del repository (ej: UserRepository) o deja vacio para todos:
set /p repo_name="Repository: "

if "%repo_name%"=="" (
    docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --tests *RepositoryTest"
) else (
    docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --tests %repo_name%Test"
)

echo.
echo Presiona cualquier tecla para volver al menu...
pause >nul
goto menu

:view_reports
cls
echo ========================================
echo   Abriendo reportes HTML...
echo ========================================
echo.

if exist "backend\build\reports\tests\test\index.html" (
    start backend\build\reports\tests\test\index.html
    echo Reportes abiertos en tu navegador!
) else (
    echo ERROR: No se encontraron reportes.
    echo Ejecuta los tests primero (opcion 1 o 2^).
)

echo.
echo Presiona cualquier tecla para volver al menu...
pause >nul
goto menu

:clean_reports
cls
echo ========================================
echo   Limpiando reportes antiguos...
echo ========================================
echo.

if exist "backend\build\reports" (
    rmdir /s /q "backend\build\reports"
    echo Reportes eliminados!
)

if exist "backend\build\test-results" (
    rmdir /s /q "backend\build\test-results"
    echo Resultados de tests eliminados!
)

echo.
echo Presiona cualquier tecla para volver al menu...
pause >nul
goto menu

:view_logs
cls
echo ========================================
echo   Ver logs de tests en tiempo real
echo ========================================
echo.
echo Presiona Ctrl+C para detener...
echo.
docker-compose -f docker-compose.test.yml logs -f backend-test
goto menu

:watch_mode
cls
echo ========================================
echo   Modo continuo (watch mode)
echo ========================================
echo.
echo Los tests se ejecutaran automaticamente al detectar cambios.
echo Presiona Ctrl+C para detener...
echo.
docker-compose -f docker-compose.test.yml run --rm backend-test sh -c "./gradlew test --continuous"
goto menu

:end
cls
echo.
echo Hasta luego!
echo.
exit /b 0
