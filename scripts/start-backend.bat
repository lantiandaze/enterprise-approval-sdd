@echo off
setlocal

cd /d "%~dp0\.."

if not exist ".\tools\apache-maven-3.9.9\bin\mvn.cmd" (
  echo Maven tools not found. Use the Windows devkit release package, or install Maven locally.
  exit /b 1
)

".\tools\apache-maven-3.9.9\bin\mvn.cmd" -f ".\backend\pom.xml" spring-boot:run "-Dspring-boot.run.profiles=postgres"
