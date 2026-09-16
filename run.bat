@echo off
title ERRORCab - Indian Cab Ride Booking & Tracking System
echo =======================================================
echo   ERRORCab - Indian Cab Ride Booking & Tracking System
echo   Team: ERROR  ^|  Tagline: "Book Smart. Ride Safe."
echo =======================================================
echo.

set "JAVA_HOME=C:\Users\azelh\tools\jdk-21.0.12.1+1"
set "PATH=%JAVA_HOME%\bin;C:\Users\azelh\tools\apache-maven-3.9.6\bin;%PATH%"

echo Starting ERRORCab Spring Boot Application on port 8080...
echo.
echo Access URLs:
echo   - Local Desktop: http://localhost:8080
echo   - Mobile / Wi-Fi: Connect phone to same Wi-Fi and open:
echo                    http://^<YOUR-COMPUTER-IP^>:8080
echo.
echo Demo Accounts (Password: password123):
echo   - Passenger: passenger@example.com
echo   - Driver:    driver@example.com
echo   - Admin:     admin@example.com
echo =======================================================
echo.

if exist "target\errorcab-1.0.0.jar" (
    java -jar target\errorcab-1.0.0.jar
) else (
    mvn spring-boot:run
)

pause
