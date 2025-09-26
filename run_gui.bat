@echo off
title Procesador de Señales Digitales - GUI

echo ===============================================
echo   PROCESADOR DE SEÑALES DIGITALES - GUI
echo ===============================================
echo.

:: Verificar si Java está instalado
java -version >nul 2>&1

if errorlevel 1 (
    echo ❌ Error: Java no está instalado.
    echo    Por favor instala Java 11 o superior.
    pause
    exit /b 1
)

:: Verificar si Maven está instalado
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Maven no está instalado.
    echo    Por favor instala Apache Maven.
    pause
    exit /b 1
)

echo 🔧 Compilando proyecto...
mvn compile

if errorlevel 1 (
    echo ❌ Error en la compilación
    echo    Revisa los mensajes de error anteriores
    pause
    exit /b 1
)

echo ✅ Compilación exitosa
echo.
echo 🚀 Iniciando interfaz gráfica...
echo.

:: Ejecutar la aplicación
mvn exec:java

pause