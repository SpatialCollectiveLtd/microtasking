# Script to create admin user in the database

$url = "http://localhost:8080/user/create-admin"
$params = @{
    id = "tech-admin-001"
    fullName = "Tech"
    email = "tech@spatialcollective.com"
}

Write-Host "Creating admin user: tech@spatialcollective.com" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Body $params -ContentType "application/x-www-form-urlencoded"
    Write-Host "SUCCESS: Admin user created!" -ForegroundColor Green
    Write-Host "User Details:" -ForegroundColor Yellow
    $response | ConvertTo-Json
} catch {
    Write-Host "ERROR: Failed to create admin user" -ForegroundColor Red
    Write-Host $_.Exception.Message
    Write-Host "Make sure the backend server is running on localhost:8080"
}
