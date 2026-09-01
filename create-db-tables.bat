@echo off
setlocal enabledelayedexpansion

REM Use this for a clean MariaDB/MySQL install of the Bowling schema.
REM Set DB_USERNAME and DB_PASSWORD as environment vars or edit the defaults below.

IF NOT DEFINED DB_USERNAME set "DB_USERNAME=root"
IF NOT DEFINED DB_PASSWORD set "DB_PASSWORD=qwer1234"

set "DB_HOST=localhost"
set "DB_PORT=3306"
set "CLIENT_PATH="
set "CLIENT_NAME="

REM Optional override for nonstandard installs, e.g.:
REM   set DB_CLIENT=C:\Program Files\MariaDB\bin\mariadb.exe
IF NOT DEFINED DB_CLIENT set "DB_CLIENT="
if defined DB_CLIENT (
    set "CLIENT_PATH=%DB_CLIENT%"
    if /i "%DB_CLIENT:~-12%"=="mariadb.exe" (
        set "CLIENT_NAME=mariadb"
    ) else (
        set "CLIENT_NAME=mysql"
    )
)

if not defined CLIENT_PATH call :find_client
if not defined CLIENT_PATH (
    echo MariaDB/MySQL client not found in PATH or in common Windows install folders.
    echo Install MariaDB/MySQL client or add its bin folder to PATH.
    echo Or set DB_CLIENT to the full path to mysql.exe or mariadb.exe, for example:
    echo   set DB_CLIENT=C:\Program Files\MariaDB\bin\mariadb.exe
    echo Then rerun this script.
    exit /b 1
)

cd /d "%~dp0"

echo Creating Bowling database tables...

if /i "%CLIENT_NAME%"=="mariadb" (
    "%CLIENT_PATH%" -h %DB_HOST% -P %DB_PORT% -u%DB_USERNAME% -p%DB_PASSWORD% < "%~dp0create-db-tables.sql"
) else (
    "%CLIENT_PATH%" -h %DB_HOST% -P %DB_PORT% -u%DB_USERNAME% -p%DB_PASSWORD% < "%~dp0create-db-tables.sql"
)

if errorlevel 1 (
    echo.
    echo Failed to create the database schema.
    echo Check your MariaDB/MySQL server is running and DB_USERNAME / DB_PASSWORD are correct.
    exit /b 1
)

echo.
echo Schema created successfully.
echo Tables: users, role, user_roles, bowler, bowling_session, game, bowler_game, frame
pause

goto :eof

:find_client
where mariadb >nul 2>nul
if not errorlevel 1 (
    set "CLIENT_NAME=mariadb"
    for /f "delims=" %%I in ('where mariadb 2^>nul') do set "CLIENT_PATH=%%I"
    exit /b 0
)

where mysql >nul 2>nul
if not errorlevel 1 (
    set "CLIENT_NAME=mysql"
    for /f "delims=" %%I in ('where mysql 2^>nul') do set "CLIENT_PATH=%%I"
    exit /b 0
)

for %%D in (
    "C:\Program Files\MariaDB\MariaDB Server\bin"
    "C:\Program Files\MariaDB\bin"
    "C:\Program Files\MySQL\MySQL Server\bin"
    "C:\xampp\mysql\bin"
    "C:\xampp\mariadb\bin"
) do (
    if exist "%%~D\mariadb.exe" (
        set "CLIENT_NAME=mariadb"
        set "CLIENT_PATH=%%~D\mariadb.exe"
        exit /b 0
    )
    if exist "%%~D\mysql.exe" (
        set "CLIENT_NAME=mysql"
        set "CLIENT_PATH=%%~D\mysql.exe"
        exit /b 0
    )
)

exit /b 1
