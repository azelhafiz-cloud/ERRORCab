Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "  ERRORCab - Indian Cab Ride Booking & Tracking System" -ForegroundColor Yellow
Write-Host "  Team: ERROR | Tagline: `"Book Smart. Ride Safe.`"" -ForegroundColor Green
Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Starting ERRORCab Spring Boot Application on port 8080..." -ForegroundColor White

$env:JAVA_HOME = "C:\Users\azelh\tools\jdk-21.0.12.1+1"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\azelh\tools\apache-maven-3.9.6\bin;$env:PATH"

$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.InterfaceAlias -notlike "*Loopback*" -and $_.IPAddress -notlike "169.*" } | Select-Object -First 1).IPAddress

Write-Host "Access URLs:" -ForegroundColor Cyan
Write-Host "  - Local Desktop:   http://localhost:8080" -ForegroundColor Green
if ($ip) {
    Write-Host "  - Mobile / Wi-Fi:  http://$($ip):8080" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "Demo Credentials (Password: password123):" -ForegroundColor Cyan
Write-Host "  - Passenger: passenger@example.com" -ForegroundColor White
Write-Host "  - Driver:    driver@example.com" -ForegroundColor White
Write-Host "  - Admin:     admin@example.com" -ForegroundColor White
Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host ""

if (Test-Path "target\errorcab-1.0.0.jar") {
    java -jar target\errorcab-1.0.0.jar
} else {
    mvn spring-boot:run
}
