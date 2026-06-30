@echo off
echo Checking port 8080...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    set PID=%%a
)

if not defined PID (
    echo No process is using port 8080.
    goto :end
)

echo Found process on port 8080 with PID %PID%
taskkill /PID %PID% /F
if %errorlevel% == 0 (
    echo Process %PID% killed successfully.
) else (
    echo Failed to kill process %PID%. Try running as Administrator.
)

:end
pause
