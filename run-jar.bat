@echo off
title ERRORCab - Packaged Production JAR Launcher
echo =======================================================
echo   ERRORCab - Production JAR Launcher
echo   Team: ERROR  ^|  Tagline: "Book Smart. Ride Safe."
echo =======================================================
echo.

set "JAVA_HOME=C:\Users\azelh\tools\jdk-21.0.12.1+1"
set "PATH=%JAVA_HOME%\bin;C:\Users\azelh\tools\apache-maven-3.9.6\bin;%PATH%"

echo Building latest project JAR...
call mvn package -DskipTests
if errorlevel 1 (
    echo [ERROR] Maven build failed. Unable to package JAR.
    pause
    exit /b 1
)

if exist "target\errorcab-1.0.0.jar" (
    echo Starting latest ERRORCab JAR...
    java -jar target\errorcab-1.0.0.jar
) else (
    echo [ERROR] target\errorcab-1.0.0.jar not found after packaging.
)

pause
