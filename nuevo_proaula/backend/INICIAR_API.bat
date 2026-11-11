@echo off
title API WiNet Control - Puerto 8080
color 0A
cd /d "%~dp0"

echo =========================================================
echo   API WiNet Control - Modulo de Gestion de Clientes
echo   (Issues #8 - Gyanela Meza)
echo =========================================================
echo.

if not exist bin mkdir bin

echo [1/2] Compilando el backend...
javac -encoding UTF-8 -cp "lib/*" -d bin src\*.java src\controladores\*.java src\entidades\*.java src\metodos\*.java src\modelos\*.java src\utils\*.java

if errorlevel 1 (
    echo.
    echo  ERROR: no se pudo compilar. Revisa los mensajes de arriba.
    echo.
    pause
    exit /b 1
)

echo      Compilacion correcta.
echo.
echo [2/2] Iniciando el servidor...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do taskkill /F /PID %%a >nul 2>&1
echo      Cuando veas "escuchando en http://localhost:8080", la API esta lista.
echo      Para detenerla: cierra esta ventana o pulsa Ctrl + C.
echo.

java -cp "bin;lib/*" ApiServer

echo.
echo El servidor se ha detenido.
pause
