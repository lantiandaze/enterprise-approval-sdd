@echo off
setlocal

cd /d "%~dp0\.."

set "PG_BIN=%CD%\tools\pgsql\bin"
set "PGDATA=%CD%\tools\pgdata"
set "PGLOG=%CD%\tools\postgres.log"

if not exist "%PG_BIN%\pg_ctl.exe" (
  echo PostgreSQL tools not found: %PG_BIN%
  echo Use the Windows devkit release package, or install PostgreSQL locally.
  exit /b 1
)

if not exist "%PGDATA%\PG_VERSION" (
  echo Initializing PostgreSQL data directory...
  "%PG_BIN%\initdb.exe" -D "%PGDATA%" -U postgres --auth=trust --encoding=UTF8 --locale=C
  if errorlevel 1 exit /b 1
)

echo Starting PostgreSQL...
"%PG_BIN%\pg_ctl.exe" -D "%PGDATA%" -l "%PGLOG%" start
if errorlevel 1 exit /b 1

echo Waiting for PostgreSQL...
for /l %%i in (1,1,20) do (
  "%PG_BIN%\psql.exe" -h 127.0.0.1 -p 5432 -U postgres -d postgres -c "SELECT 1" >nul 2>nul
  if not errorlevel 1 goto ready
  timeout /t 1 /nobreak >nul
)

echo PostgreSQL did not become ready in time.
exit /b 1

:ready
"%PG_BIN%\createdb.exe" -h 127.0.0.1 -p 5432 -U postgres enterprise_approval >nul 2>nul
echo PostgreSQL is ready. Database: enterprise_approval
