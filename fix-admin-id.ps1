# Fix Admin User ID to match Google OAuth sub claim
# The backend looks up users by Google's 'sub' claim from JWT token
# We need to update the admin user ID from 'tech-admin-001' to the actual Google sub ID

$googleSubId = "118192627616311179805"  # From JWT token
$email = "tech@spatialcollective.com"
$fullName = "Tech"

Write-Host "=== FIXING ADMIN USER ID ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Updating admin user ID from 'tech-admin-001' to '$googleSubId'" -ForegroundColor Yellow
Write-Host ""

$body = @{
    id = $googleSubId
    fullName = $fullName
    email = $email
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/user/create-admin?id=$googleSubId&fullName=$fullName&email=$email" -Method Post -ContentType "application/json"
    
    Write-Host "SUCCESS! Admin user created/updated:" -ForegroundColor Green
    Write-Host "  ID: $($response.id)" -ForegroundColor White
    Write-Host "  Name: $($response.fullName)" -ForegroundColor White
    Write-Host "  Email: $($response.email)" -ForegroundColor White
    Write-Host "  Role: $($response.role)" -ForegroundColor White
    Write-Host ""
    Write-Host "Now you can login with tech@spatialcollective.com via Google OAuth!" -ForegroundColor Green
    
} catch {
    Write-Host "ERROR: Failed to create admin user" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure the backend is running on http://localhost:8080" -ForegroundColor Yellow
}

Write-Host ""
Read-Host "Press Enter to continue"
