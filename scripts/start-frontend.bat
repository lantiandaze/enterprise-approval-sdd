@echo off
setlocal

cd /d "%~dp0\..\frontend"

call npm.cmd install
if errorlevel 1 exit /b 1

call npm.cmd run dev
