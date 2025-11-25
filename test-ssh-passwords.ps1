# SSH Credential Tester
# This script helps test different password variations

$ServerIP = "102.220.23.109"
$Username = "admin"

Write-Host "=== SSH Credential Systematic Test ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Server: $ServerIP" -ForegroundColor White
Write-Host "User: $Username" -ForegroundColor White
Write-Host ""

# List of password variations to try
$passwords = @(
    "DPw!@2025",      # Original admin password
    "DPW!@2025!",     # Root password (maybe works for admin too)
    "DPw!@2025!",     # Admin password with trailing !
    "DPW!@2025",      # All caps version
    "dpw!@2025",      # All lowercase
    "DPw@2025",       # Without first !
    "DPw!2025",       # Without @
    "DPw2025"         # No special chars
)

Write-Host "I'll show you each password to test." -ForegroundColor Yellow
Write-Host "Copy it EXACTLY and paste when SSH asks." -ForegroundColor Yellow
Write-Host ""

for ($i = 0; $i -lt $passwords.Count; $i++) {
    Write-Host "[$($i+1)/$($passwords.Count)] Testing password variation:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "PASSWORD TO TRY:" -ForegroundColor Green
    Write-Host $passwords[$i] -ForegroundColor Yellow -BackgroundColor DarkGray
    Write-Host ""
    
    # Copy to clipboard
    $passwords[$i] | Set-Clipboard
    Write-Host "✓ Copied to clipboard (Ctrl+V to paste)" -ForegroundColor Green
    Write-Host ""
    Write-Host "Run this command in a new terminal:" -ForegroundColor White
    Write-Host "ssh $Username@$ServerIP" -ForegroundColor Cyan
    Write-Host ""
    
    $result = Read-Host "Did it work? (y/n/skip)"
    
    if ($result -eq 'y') {
        Write-Host ""
        Write-Host "SUCCESS! Working password:" -ForegroundColor Green
        Write-Host $passwords[$i] -ForegroundColor Yellow -BackgroundColor DarkGray
        Write-Host ""
        Write-Host "Updating SSH config..." -ForegroundColor Cyan
        
        # Update the connect script with working password
        $workingPassword = $passwords[$i]
        Write-Host ""
        Write-Host "Save this password for future use:" -ForegroundColor Yellow
        Write-Host "Server: $ServerIP" -ForegroundColor White
        Write-Host "User: $Username" -ForegroundColor White
        Write-Host "Password: $workingPassword" -ForegroundColor White
        
        exit
    }
    elseif ($result -eq 'skip') {
        Write-Host "Skipping to next..." -ForegroundColor Gray
        continue
    }
    
    Write-Host "Password failed. Trying next variation..." -ForegroundColor Red
    Write-Host ""
}

Write-Host ""
Write-Host "=== All Variations Tested ===" -ForegroundColor Red
Write-Host ""
Write-Host "None of the password variations worked." -ForegroundColor Yellow
Write-Host ""
Write-Host "Possible issues:" -ForegroundColor Cyan
Write-Host "1. Wrong IP address (is 102.220.23.109 correct?)" -ForegroundColor White
Write-Host "2. Firewall blocking SSH" -ForegroundColor White
Write-Host "3. SSH server not configured for password auth" -ForegroundColor White
Write-Host "4. Account locked after too many attempts" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Green
Write-Host "- Verify the IP address is correct" -ForegroundColor White
Write-Host "- Check if you have console/VNC access" -ForegroundColor White
Write-Host "- Contact server administrator" -ForegroundColor White
Write-Host "- Check server management panel for SSH settings" -ForegroundColor White
