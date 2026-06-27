@echo off
echo === Bowling OKF Docs - Generating HTML + diagram ===
echo.

cd /d "%~dp0"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0generate_docs.ps1"

echo.
echo Architecture diagram is at okf\architecture.svg
pause
