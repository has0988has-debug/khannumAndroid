@echo off
setlocal
set "APP_HOME=%~dp0"
set "CACHE=%USERPROFILE%\.gradle\wrapper\dists\gradle-8.9-bin\khannum"
set "DIST=%CACHE%\gradle-8.9\bin\gradle.bat"
if exist "%DIST%" goto run
if not exist "%CACHE%" mkdir "%CACHE%"
set "ZIP=%CACHE%\gradle-8.9-bin.zip"
where powershell >nul 2>nul
if errorlevel 1 (
  echo PowerShell is required to download Gradle 8.9.
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-8.9-bin.zip' -OutFile '%ZIP%'"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%CACHE%'"
:run
call "%DIST%" %*
endlocal
