@echo off
echo === Bowling Score API - Running Tests ===
echo.

cd /d "%~dp0"

IF EXIST mvnw.cmd (
    call mvnw.cmd test
) ELSE (
    mvn test
)
pause
