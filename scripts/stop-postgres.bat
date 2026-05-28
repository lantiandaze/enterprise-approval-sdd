@echo off
setlocal

cd /d "%~dp0\.."

set "PG_BIN=%CD%\tools\pgsql\bin"
set "PGDATA=%CD%\tools\pgdata"

if not exist "%PG_BIN%\pg_ctl.exe" (
  echo PostgreSQL tools not found: %PG_BIN%
  exit /b 1
)

if not exist "%PGDATA%\PG_VERSION" (
  echo PostgreSQL data directory not found: %PGDATA%
  exit /b 0
)

"%PG_BIN%\pg_ctl.exe" -D "%PGDATA%" stop
