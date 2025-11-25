# SSH Key Setup Script
# This script sets up SSH key-based authentication for the DPW server

param(
    [switch]$Force
)

$ServerIP = "102.220.23.109"
$Username = "admin"
$Password = "DPw!@2025"

Write-Host "=== SSH Key Setup for DPW Server ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check for existing SSH keys
$sshDir = "$env:USERPROFILE\.ssh"
$keyPath = "$sshDir\id_rsa"
$pubKeyPath = "$sshDir\id_rsa.pub"

if (!(Test-Path $sshDir)) {
    Write-Host "[1/4] Creating .ssh directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $sshDir -Force | Out-Null
    Write-Host "  ✓ Created $sshDir" -ForegroundColor Green
} else {
    Write-Host "[1/4] .ssh directory exists" -ForegroundColor Green
}

# Step 2: Generate SSH key if needed
Write-Host "[2/4] Checking for SSH keys..." -ForegroundColor Yellow
if ((Test-Path $keyPath) -and !$Force) {
    Write-Host "  ✓ SSH key already exists: $keyPath" -ForegroundColor Green
    Write-Host "  (Use -Force to regenerate)" -ForegroundColor Gray
} else {
    Write-Host "  Generating new SSH key pair..." -ForegroundColor Yellow
    ssh-keygen -t rsa -b 4096 -f $keyPath -N '""' -C "tech@dpw-server"
    Write-Host "  ✓ SSH key generated" -ForegroundColor Green
}

# Step 3: Display public key
Write-Host "[3/4] Your public key:" -ForegroundColor Yellow
if (Test-Path $pubKeyPath) {
    $publicKey = Get-Content $pubKeyPath
    Write-Host ""
    Write-Host $publicKey -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "  ✗ Public key not found!" -ForegroundColor Red
    exit
}

# Step 4: Instructions to add key to server
Write-Host "[4/4] Next steps to enable key-based authentication:" -ForegroundColor Yellow
Write-Host ""
Write-Host "OPTION A: Automatic (requires sshpass or plink)" -ForegroundColor Green
Write-Host "  Run these commands:" -ForegroundColor White
Write-Host ""
Write-Host "  # Copy public key to server" -ForegroundColor Gray
Write-Host "  type $pubKeyPath | ssh admin@102.220.23.109 `"mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys`"" -ForegroundColor Yellow
Write-Host ""
Write-Host "OPTION B: Manual (most reliable)" -ForegroundColor Green
Write-Host "  1. Copy the public key above (Ctrl+C)" -ForegroundColor White
Write-Host "  2. SSH to server: ssh admin@102.220.23.109" -ForegroundColor White
Write-Host "  3. Enter password: DPw!@2025" -ForegroundColor White
Write-Host "  4. Run these commands on the server:" -ForegroundColor White
Write-Host ""
Write-Host "     mkdir -p ~/.ssh" -ForegroundColor Yellow
Write-Host "     chmod 700 ~/.ssh" -ForegroundColor Yellow
Write-Host "     nano ~/.ssh/authorized_keys" -ForegroundColor Yellow
Write-Host ""
Write-Host "  5. Paste your public key in the editor" -ForegroundColor White
Write-Host "  6. Press Ctrl+X, then Y, then Enter to save" -ForegroundColor White
Write-Host "  7. Set permissions:" -ForegroundColor White
Write-Host ""
Write-Host "     chmod 600 ~/.ssh/authorized_keys" -ForegroundColor Yellow
Write-Host "     exit" -ForegroundColor Yellow
Write-Host ""
Write-Host "  8. Test connection: ssh admin@102.220.23.109" -ForegroundColor White
Write-Host "     (should NOT ask for password)" -ForegroundColor Gray
Write-Host ""
Write-Host "OPTION C: Copy public key to clipboard" -ForegroundColor Green
$publicKey | Set-Clipboard
Write-Host "  ✓ Public key copied to clipboard!" -ForegroundColor Green
Write-Host "  Just paste it into ~/.ssh/authorized_keys on the server" -ForegroundColor White
Write-Host ""
