@echo off
setlocal

set DIR=%~dp0

if exist "%DIR%gradle\wrapper\gradle-wrapper.jar" (
    call "%DIR%gradlew" %*
    exit /b %ERRORLEVEL%
)

where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Gradle wrapper JAR missing; using system Gradle fallback...
    gradle %*
    exit /b %ERRORLEVEL%
)

echo Gradle is not installed and wrapper JAR is missing.
echo Please run: gradle wrapper --gradle-version=8.5
exit /b 1
