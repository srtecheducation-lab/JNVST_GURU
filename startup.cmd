@echo off
setlocal

set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "DATABASE_ENABLED=false"

cd /d "%~dp0"

if "%1"=="start" goto start_app
if "%1"=="status" goto status_app
if "%1"=="stop" goto stop_app
if "%1"=="" goto start_app

echo Usage:
	echo   startup.cmd start   - start PostgreSQL and the backend
	echo   startup.cmd status  - check if the app is running
	echo   startup.cmd stop    - stop the app and PostgreSQL container
exit /b 1

:start_app
if "%DATABASE_ENABLED%"=="true" (
    where docker >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        echo Docker not found in PATH. PostgreSQL container startup will be skipped.
        echo If Docker is installed but not on PATH, add it and re-run this script.
        goto run_app
    )

    echo Starting PostgreSQL via Docker Compose...
    docker compose up -d postgres
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to start PostgreSQL using Docker Compose.
        echo Continuing without PostgreSQL startup. Make sure PostgreSQL is available and configured.
        goto run_app
    )
) else (
    echo DATABASE_ENABLED=false; skipping PostgreSQL startup.
    echo The backend will start with the in-memory test database profile.
)

:run_app
echo Starting JNVST GURU backend...
if "%DATABASE_ENABLED%"=="true" (
    call .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
) else (
    call .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
)
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

if "%DATABASE_ENABLED%"=="true" (
    where docker >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        docker ps --format "table {{.Names}}\t{{.Status}}" | findstr /I "jnvst-guru-postgres"
        if %ERRORLEVEL% NEQ 0 (
            echo PostgreSQL container is not running.
        )
    )
) else (
    echo DATABASE_ENABLED=false; PostgreSQL is intentionally disabled.
)
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

if "%DATABASE_ENABLED%"=="true" (
    where docker >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo Stopping PostgreSQL container...
        docker compose down
    ) else (
        echo Docker not found; PostgreSQL container was not stopped.
    )
) else (
    echo DATABASE_ENABLED=false; PostgreSQL was not started by the script.
)
exit /b 0

endlocal
