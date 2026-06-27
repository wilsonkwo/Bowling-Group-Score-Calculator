@echo off
setlocal enabledelayedexpansion

REM Refresh PATH from the registry in case Node/Java were installed after this
REM shell/session started (common right after a fresh install).
for /f "usebackq tokens=2,*" %%A in (`reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul`) do set "SYS_PATH=%%B"
for /f "usebackq tokens=2,*" %%A in (`reg query "HKCU\Environment" /v Path 2^>nul`) do set "USER_PATH=%%B"
if defined SYS_PATH if defined USER_PATH set "PATH=!SYS_PATH!;!USER_PATH!"

echo === Bowling Score App - Backend + Frontend ===
echo.

REM Set DB credentials here for local dev (or set them as system env vars)
IF NOT DEFINED DB_USERNAME set DB_USERNAME=root
IF NOT DEFINED DB_PASSWORD set DB_PASSWORD=qwer1234

REM Optional: override JWT secret for local dev
IF NOT DEFINED APP_JWT_SECRET set APP_JWT_SECRET=local-dev-secret-change-in-production-32c

cd /d "%~dp0"
if not exist logs mkdir logs

echo Starting backend on http://localhost:8080 (log: logs\backend.log)
echo DB: %DB_USERNAME%@localhost:3306/bowling
start /B "" cmd /c ""%~dp0run-backend.bat" > "%~dp0logs\backend.log" 2>&1"

echo.
echo Starting frontend on http://localhost:5173 (log: logs\frontend.log)
IF NOT EXIST "frontend\node_modules" (
    echo Installing frontend dependencies first - this may take a minute...
    pushd frontend
    call npm install
    popd
)
start /B "" cmd /c ""%~dp0frontend\run-frontend.bat" > "%~dp0logs\frontend.log" 2>&1"

echo.
echo Both servers are starting in the background.
echo Backend:  http://localhost:8080  (tail logs\backend.log)
echo Frontend: http://localhost:5173  (tail logs\frontend.log)
echo Run stop.bat to stop both.
echo.
echo Keep this window open - closing it stops the servers.
pause
