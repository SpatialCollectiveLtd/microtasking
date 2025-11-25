# SSH Connection Troubleshooting Script
# This script helps diagnose and fix SSH connection issues

$ServerIP = "102.220.23.109"
$Username = "admin"
$Password = "DPw!@2025"

Write-Host "=== SSH Connection Troubleshooting ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check if server is reachable
Write-Host "[1/5] Testing server connectivity..." -ForegroundColor Yellow
$ping = Test-Connection -ComputerName $ServerIP -Count 2 -Quiet
if ($ping) {
    Write-Host "  ✓ Server is reachable" -ForegroundColor Green
} else {
    Write-Host "  ✗ Server is NOT reachable" -ForegroundColor Red
    exit
}

# Test 2: Check if SSH port is open
Write-Host "[2/5] Testing SSH port 22..." -ForegroundColor Yellow
$tcpTest = Test-NetConnection -ComputerName $ServerIP -Port 22 -WarningAction SilentlyContinue
if ($tcpTest.TcpTestSucceeded) {
    Write-Host "  ✓ SSH port 22 is open" -ForegroundColor Green
} else {
    Write-Host "  ✗ SSH port 22 is closed" -ForegroundColor Red
    exit
}

# Test 3: Check SSH client
Write-Host "[3/5] Checking SSH client..." -ForegroundColor Yellow
$sshClient = Get-Command ssh -ErrorAction SilentlyContinue
if ($sshClient) {
    Write-Host "  ✓ OpenSSH client found: $($sshClient.Source)" -ForegroundColor Green
} else {
    Write-Host "  ✗ OpenSSH client not found" -ForegroundColor Red
    Write-Host "  Installing OpenSSH Client..." -ForegroundColor Yellow
    Add-WindowsCapability -Online -Name OpenSSH.Client~~~~0.0.1.0
}

# Test 4: Explain the VS Code Remote SSH issue
Write-Host "[4/5] Diagnosing VS Code Remote SSH issue..." -ForegroundColor Yellow
Write-Host ""
Write-Host "  ISSUE: VS Code Remote SSH expects key-based authentication by default." -ForegroundColor Cyan
Write-Host "  Your server is configured for password authentication." -ForegroundColor Cyan
Write-Host ""
Write-Host "  SOLUTIONS:" -ForegroundColor White
Write-Host ""
Write-Host "  Option A: Use Password Authentication in VS Code" -ForegroundColor Green
Write-Host "  ----------------------------------------" -ForegroundColor Gray
Write-Host "  1. Open VS Code Command Palette (Ctrl+Shift+P)"
Write-Host "  2. Type: 'Remote-SSH: Settings'"
Write-Host "  3. Find 'Show Login Terminal' and ENABLE it"
Write-Host "  4. Try connecting again - it will prompt for password"
Write-Host ""
Write-Host "  Option B: Set Up SSH Key (More Secure)" -ForegroundColor Green
Write-Host "  ----------------------------------------" -ForegroundColor Gray
Write-Host "  Run this script with -SetupKey parameter"
Write-Host "  Example: .\fix-ssh-connection.ps1 -SetupKey"
Write-Host ""
Write-Host "  Option C: Use SSH Config File" -ForegroundColor Green
Write-Host "  ----------------------------------------" -ForegroundColor Gray
Write-Host "  I can create an SSH config for you (see below)"
Write-Host ""

# Test 5: Create SSH config for easier connection
Write-Host "[5/5] Creating SSH config file..." -ForegroundColor Yellow
$sshDir = "$env:USERPROFILE\.ssh"
if (!(Test-Path $sshDir)) {
    New-Item -ItemType Directory -Path $sshDir -Force | Out-Null
}

$configPath = "$sshDir\config"
$configEntry = @"

# DPW Server Configuration
Host dpw-server
    HostName 102.220.23.109
    User admin
    Port 22
    PreferredAuthentications password
    PubkeyAuthentication no
    PasswordAuthentication yes
    StrictHostKeyChecking no
    UserKnownHostsFile /dev/null
"@

# Check if config exists and append
if (Test-Path $configPath) {
    # Check if entry already exists
    $existingConfig = Get-Content $configPath -Raw
    if ($existingConfig -notmatch 'Host dpw-server') {
        Add-Content -Path $configPath -Value $configEntry
        Write-Host "  ✓ Added DPW server to SSH config" -ForegroundColor Green
    } else {
        Write-Host "  ✓ DPW server already in SSH config" -ForegroundColor Green
    }
} else {
    Set-Content -Path $configPath -Value $configEntry.TrimStart()
    Write-Host "  ✓ Created SSH config with DPW server" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== NEXT STEPS ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "TRY THIS IN VS CODE:" -ForegroundColor Green
Write-Host "  1. Open Command Palette (Ctrl+Shift+P)" -ForegroundColor White
Write-Host "  2. Select: Remote-SSH: Connect to Host..." -ForegroundColor White
Write-Host "  3. Choose: dpw-server" -ForegroundColor White
Write-Host "  4. When prompted, enter password: DPw!@2025" -ForegroundColor White
Write-Host ""
Write-Host "OR CONFIGURE VS CODE SETTINGS:" -ForegroundColor Green
Write-Host "  File → Preferences → Settings" -ForegroundColor White
Write-Host "  Search: 'remote.SSH.showLoginTerminal'" -ForegroundColor White
Write-Host "  Check the box to enable it" -ForegroundColor White
Write-Host ""
Write-Host "OR TEST FROM COMMAND LINE:" -ForegroundColor Green
Write-Host "  ssh dpw-server" -ForegroundColor Yellow
Write-Host "  (or)" -ForegroundColor White
Write-Host "  ssh admin@102.220.23.109" -ForegroundColor Yellow
Write-Host ""
