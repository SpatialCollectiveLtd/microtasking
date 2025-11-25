# Test DPW Server Connection
# IP: 102.220.23.109
# Admin User: admin / DPw!@2025
# Root User: root / DPW!@2025!

$serverIP = "102.220.23.109"
$adminUser = "admin"

Write-Host "=== TESTING DPW SERVER CONNECTION ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Server IP: $serverIP" -ForegroundColor Yellow
Write-Host "Admin User: $adminUser" -ForegroundColor Yellow
Write-Host ""

# Test 1: Ping server
Write-Host "Test 1: Pinging server..." -ForegroundColor White
try {
    $ping = Test-Connection -ComputerName $serverIP -Count 4 -ErrorAction Stop
    Write-Host "  ✓ Server is reachable" -ForegroundColor Green
    Write-Host "    Average response time: $([math]::Round(($ping | Measure-Object -Property ResponseTime -Average).Average, 2))ms" -ForegroundColor White
} catch {
    Write-Host "  ✗ Server is NOT reachable" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Check common ports
Write-Host "Test 2: Checking common ports..." -ForegroundColor White

$ports = @(
    @{Name="SSH (22)"; Port=22},
    @{Name="HTTP (80)"; Port=80},
    @{Name="HTTPS (443)"; Port=443},
    @{Name="MySQL (3306)"; Port=3306},
    @{Name="Custom SSH (2222)"; Port=2222},
    @{Name="PostgreSQL (5432)"; Port=5432}
)

foreach ($portInfo in $ports) {
    $portName = $portInfo.Name
    $port = $portInfo.Port
    
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    try {
        $connect = $tcpClient.BeginConnect($serverIP, $port, $null, $null)
        $wait = $connect.AsyncWaitHandle.WaitOne(2000, $false)
        
        if ($wait -and $tcpClient.Connected) {
            Write-Host "  ✓ Port $port ($portName) is OPEN" -ForegroundColor Green
            $tcpClient.Close()
        } else {
            Write-Host "  ✗ Port $port ($portName) is CLOSED or filtered" -ForegroundColor Red
            $tcpClient.Close()
        }
    } catch {
        Write-Host "  ✗ Port $port ($portName) is CLOSED or filtered" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== NEXT STEPS ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "If SSH port (22 or 2222) is open:" -ForegroundColor Yellow
Write-Host "  1. Use PuTTY, MobaXterm, or Windows Terminal to connect" -ForegroundColor White
Write-Host "  2. Connect to: ssh admin@$serverIP" -ForegroundColor White
Write-Host "  3. Password: DPw!@2025" -ForegroundColor White
Write-Host "  4. After login, switch to root: su root" -ForegroundColor White
Write-Host "  5. Root password: DPW!@2025!" -ForegroundColor White
Write-Host ""
Write-Host "If SSH is not accessible:" -ForegroundColor Yellow
Write-Host "  - Contact your hosting provider to:" -ForegroundColor White
Write-Host "    • Confirm the server IP address" -ForegroundColor White
Write-Host "    • Verify SSH is enabled and the port number" -ForegroundColor White
Write-Host "    • Check firewall rules allow your IP" -ForegroundColor White
Write-Host "    • Confirm credentials are correct" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to continue"
