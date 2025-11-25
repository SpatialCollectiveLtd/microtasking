# Check Backend Application Status

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Backend Application Status Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if application is running
Write-Host "Checking if Spring Boot is running on port 8080..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Backend is RUNNING and responding!" -ForegroundColor Green
    Write-Host "  Status Code: $($response.StatusCode)" -ForegroundColor White
}
catch {
    if ($_.Exception.Message -like "*Connection refused*" -or $_.Exception.Message -like "*Unable to connect*") {
        Write-Host "✗ Backend is NOT running" -ForegroundColor Red
        Write-Host "  Run: gradlew.bat bootRun" -ForegroundColor Yellow
    }
    elseif ($_.Exception.Message -like "*404*") {
        Write-Host "✓ Backend is RUNNING (404 is expected for root path)" -ForegroundColor Green
    }
    else {
        Write-Host "? Backend status unclear: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host ""

# Test a specific API endpoint
Write-Host "Testing API endpoint /questions..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/questions" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    $data = $response.Content | ConvertFrom-Json
    Write-Host "✓ API is responding!" -ForegroundColor Green
    Write-Host "  Questions in database: $($data.Count)" -ForegroundColor White
}
catch {
    Write-Host "✗ API not accessible yet: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Show what to look for in logs
Write-Host "IN THE STARTUP LOGS, LOOK FOR:" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ SUCCESS INDICATORS:" -ForegroundColor Green
Write-Host "  - HikariPool-1 - Starting..." -ForegroundColor White
Write-Host "  - HikariPool-1 - Start completed." -ForegroundColor White
Write-Host "  - Hibernate: create table..." -ForegroundColor White
Write-Host "  - Started MicrotaskToolApiApplication" -ForegroundColor White
Write-Host "  - Tomcat started on port(s): 8080" -ForegroundColor White
Write-Host ""
Write-Host "✗ ERROR INDICATORS:" -ForegroundColor Red
Write-Host "  - Communications link failure" -ForegroundColor White
Write-Host "  - Access denied for user" -ForegroundColor White
Write-Host "  - Unknown database" -ForegroundColor White
Write-Host "  - Unable to create initial connections" -ForegroundColor White
Write-Host ""
