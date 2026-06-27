@echo off
echo === Bowling Score App - Stopping servers ===
echo.

cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop.ps1"

echo.
echo Done.
pause
