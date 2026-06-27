@echo off
setlocal enabledelayedexpansion
for /f "usebackq tokens=2,*" %%A in (`reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul`) do set "SYS_PATH=%%B"
for /f "usebackq tokens=2,*" %%A in (`reg query "HKCU\Environment" /v Path 2^>nul`) do set "USER_PATH=%%B"
if defined SYS_PATH if defined USER_PATH set "PATH=!SYS_PATH!;!USER_PATH!"

cd /d "%~dp0"
IF EXIST "%~dp0mvnw.cmd" (
    call "%~dp0mvnw.cmd" spring-boot:run < nul
) ELSE (
    call mvn spring-boot:run < nul
)
