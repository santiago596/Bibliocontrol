@echo off
setlocal
title Detener Bibliocontrol
set "XAMPP=C:\xampp"

echo Deteniendo la aplicacion Bibliocontrol (puerto 8080)...
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":8080 .*LISTENING"') do taskkill /PID %%P /F >nul 2>&1

echo Apagando MySQL de forma segura...
"%XAMPP%\mysql\bin\mysqladmin.exe" -uroot shutdown >nul 2>&1
if errorlevel 1 (
    echo MySQL ya estaba apagado o se administra desde el XAMPP Control Panel.
) else (
    echo MySQL apagado correctamente.
)
echo Listo.
pause