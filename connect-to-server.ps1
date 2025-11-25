# Interactive SSH Session Helper
# This opens an SSH session to the server for manual work

Write-Host "=== CONNECTING TO DPW SERVER ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Server: 102.220.23.109" -ForegroundColor White
Write-Host "User: admin" -ForegroundColor White
Write-Host "Password: DPw!@2025" -ForegroundColor White
Write-Host ""
Write-Host "After login, to switch to root:" -ForegroundColor Yellow
Write-Host "  su root" -ForegroundColor White
Write-Host "  Password: DPW!@2025!" -ForegroundColor White
Write-Host ""
Write-Host "Connecting..." -ForegroundColor Green
Write-Host ""

# Use OpenSSH (built into Windows 10/11)
ssh admin@102.210.149.40
