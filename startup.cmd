@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "DATABASE_ENABLED=false"

cd /d "%~dp0"

echo [STARTUP] Using single application.properties configuration for the real database.
if exist "local.properties" (
    echo [STARTUP] local.properties found at "%cd%\local.properties"
) else (
    echo [STARTUP] local.properties not found in "%cd%"
)

if "%1"=="start" goto start_app
if "%1"=="status" goto status_app
if "%1"=="stop" goto stop_app
if "%1"=="" goto start_app

echo Usage:
	echo   startup.cmd start   - start the backend
	echo   startup.cmd status  - check if the app is running
	echo   startup.cmd stop    - stop the backend
exit /b 1
 
:start_app
echo [STARTUP] Starting JNVST GURU backend using the single application.properties configuration.
goto run_app
 
:run_app
echo Starting JNVST GURU backend...
call .\mvnw.cmd spring-boot:run
exit /b %ERRORLEVEL%

:status_app
for /f "tokens=2 delims=:" %%A in ('netstat -ano ^| findstr ":8080 " 2^>nul') do (
    set "PID=%%A"
)
if defined PID (
    echo JNVST GURU backend is running on port 8080 with PID %PID%
    echo Process details:
    tasklist /FI "PID eq %PID%" /FO CSV /NH
) else (
    echo JNVST GURU backend is not running on port 8080.
)

echo [STARTUP] Using external database from application.properties/local.properties.
exit /b 0

:stop_app
echo Stopping JNVST GURU backend...
for /f "tokens=2 delims=:" %%A in ('netstat -ano ^| findstr ":8080 " 2^>nul') do (
    set "PID=%%A"
)
if defined PID (
    taskkill /PID %PID% /F >nul 2>&1
    echo Stopped backend process PID %PID%
) else (
    echo No backend process found on port 8080.
)

echo [STARTUP] No local PostgreSQL container is managed by this script; the app uses the configured external database.
exit /b 0

endlocal
