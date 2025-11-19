@echo off
echo ================================================
echo Haciendo commit de ForceTrack
echo ================================================
echo.

cd /d "%~dp0"

echo Agregando archivos al staging area...
git add .

echo.
echo Haciendo commit...
git commit -m "Refactorizacion completa: Limpieza de codigo, optimizacion de RequestQueue, separacion de configuracion y preparacion para release

- Eliminados todos los emojis de logs para mejor profesionalismo
- Optimizado RequestQueue con reintentos automaticos y exponential backoff
- Configuracion de API y rutas movidas a modulo config/ separado
- Solucionado error de CalendarScreen (ExperimentalFoundationApi)
- Activadas rutas de Calendar y DailyLog en MainActivity
- Actualizado .gitignore para proteger archivos sensibles (keystore, release, etc.)
- Proyecto listo para generar APK/AAB firmado
- Version: 1.0 (versionCode: 1)"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo Commit realizado exitosamente!
    echo ================================================
    echo.
    echo Archivos confirmados en el repositorio local.
    echo.
    echo Para subir a GitHub ejecuta:
    echo git push origin main
    echo.
    echo O si es la primera vez:
    echo git push -u origin main
    echo.
) else (
    echo.
    echo ================================================
    echo Error al hacer commit
    echo ================================================
    echo.
    echo Posibles causas:
    echo 1. No hay cambios para commitear
    echo 2. Git no esta configurado
    echo 3. No estas en un repositorio git
    echo.
    echo Para inicializar git ejecuta:
    echo git init
    echo git add .
    echo git commit -m "Primer commit"
    echo.
)

pause

