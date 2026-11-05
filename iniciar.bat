@echo off
setlocal
title Bibliocontrol
cd /d "%~dp0"

set "XAMPP=C:\xampp"
set "MYSQL=%XAMPP%\mysql\bin\mysql.exe"

echo ================================================
echo   Bibliocontrol - Sistema de Biblioteca
echo ================================================

rem ---------- 1. Java ----------
if not defined JAVA_HOME (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do set "JAVA_HOME=%%D"
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] No se encontro Java 17. Instale Eclipse Temurin JDK 17.
    pause
    exit /b 1
)
echo [OK] Java: %JAVA_HOME%

rem ---------- 2. MySQL de XAMPP ----------
if not exist "%MYSQL%" (
    echo [ERROR] No se encontro XAMPP en %XAMPP%.
    pause
    exit /b 1
)
netstat -ano | findstr /R /C:":3306 .*LISTENING" >nul
if errorlevel 1 (
    echo [..] Iniciando MySQL de XAMPP...
    start "MySQL XAMPP" /MIN "%XAMPP%\mysql\bin\mysqld.exe" --defaults-file="%XAMPP%\mysql\bin\my.ini" --standalone
    for /l %%i in (1,1,30) do (
        ping -n 2 127.0.0.1 >nul
        netstat -ano | findstr /R /C:":3306 .*LISTENING" >nul && goto mysql_listo
    )
    echo [ERROR] MySQL no inicio. Abra el XAMPP Control Panel y presione Start en MySQL.
    pause
    exit /b 1
)
:mysql_listo
rem Esperar a que MySQL termine de arrancar y responda consultas
for /l %%i in (1,1,30) do (
    "%MYSQL%" -uroot -e "SELECT 1" >nul 2>&1 && goto mysql_responde
    ping -n 2 127.0.0.1 >nul
)
echo [ERROR] MySQL no responde. Reinicielo desde el XAMPP Control Panel.
pause
exit /b 1
:mysql_responde
ping -n 3 127.0.0.1 >nul
echo [OK] MySQL en el puerto 3306

rem ---------- 3. Base de datos ----------
"%MYSQL%" -uroot -e "USE bibliocontrol" >nul 2>&1
if errorlevel 1 (
    echo [..] Creando la base de datos bibliocontrol...
    "%MYSQL%" -uroot --default-character-set=utf8mb4 < "database\bibliocontrol.sql"
    if errorlevel 1 (
        echo [ERROR] No se pudo importar database\bibliocontrol.sql
        pause
        exit /b 1
    )
)
echo [OK] Base de datos bibliocontrol

rem ---------- 4. Aplicacion ----------
echo [..] Iniciando la aplicacion (la primera vez descarga dependencias, puede tardar unos minutos)...
echo      Cuando aparezca "Started BibliocontrolApplication" abra http://localhost:8080
echo      Para detenerla presione Ctrl+C en esta ventana.
start "" /B powershell -NoProfile -WindowStyle Hidden -Command "for($i=0;$i -lt 300;$i++){try{Invoke-WebRequest http://localhost:8080 -UseBasicParsing -TimeoutSec 2|Out-Null;Start-Process http://localhost:8080;break}catch{Start-Sleep 2}}"
call "%~dp0mvnw.cmd" -q spring-boot:run
pause
