@echo off
setlocal

cd /d "%~dp0\..\frontend"

npm.cmd install
if errorlevel 1 exit /b 1

npm.cmd run dev
