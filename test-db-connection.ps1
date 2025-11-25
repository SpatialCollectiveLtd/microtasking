# Test MySQL Database Connection
# Run this script to verify database is accessible

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Microtasking Database Connection Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$dbHost = "169.255.58.54"
$dbPort = 3306
$dbName = "spatialcoke_dpw_microtasking_prod"
$dbUser = "spatialcoke_dpw_prod_user"
$dbPassword = "NtDcdgPoadgxT5"

Write-Host "Testing connection to:" -ForegroundColor Yellow
Write-Host "  Host: $dbHost" -ForegroundColor White
Write-Host "  Port: $dbPort" -ForegroundColor White
Write-Host "  Database: $dbName" -ForegroundColor White
Write-Host "  User: $dbUser" -ForegroundColor White
Write-Host ""

# Test 1: Check if port is accessible
Write-Host "[1/3] Testing if MySQL port is accessible..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connection = $tcpClient.BeginConnect($dbHost, $dbPort, $null, $null)
    $wait = $connection.AsyncWaitHandle.WaitOne(5000, $false)
    
    if ($wait) {
        try {
            $tcpClient.EndConnect($connection)
            Write-Host "  ✓ Port $dbPort is OPEN and accessible" -ForegroundColor Green
            $tcpClient.Close()
            $portAccessible = $true
        }
        catch {
            Write-Host "  ✗ Port is open but connection failed: $($_.Exception.Message)" -ForegroundColor Red
            $portAccessible = $false
        }
    }
    else {
        Write-Host "  ✗ Connection TIMEOUT - Port may be blocked by firewall" -ForegroundColor Red
        $tcpClient.Close()
        $portAccessible = $false
    }
}
catch {
    Write-Host "  ✗ FAILED to connect to port: $($_.Exception.Message)" -ForegroundColor Red
    $portAccessible = $false
}

Write-Host ""

# Test 2: Check if MySQL client is available
Write-Host "[2/3] Checking for MySQL client..." -ForegroundColor Yellow
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue

if ($mysqlPath) {
    Write-Host "  ✓ MySQL client found at: $($mysqlPath.Source)" -ForegroundColor Green
    $hasMysqlClient = $true
}
else {
    Write-Host "  ✗ MySQL client NOT found in PATH" -ForegroundColor Red
    Write-Host "    Download from: https://dev.mysql.com/downloads/mysql/" -ForegroundColor Yellow
    $hasMysqlClient = $false
}

Write-Host ""

# Test 3: Try to connect using MySQL client (if available)
if ($hasMysqlClient -and $portAccessible) {
    Write-Host "[3/3] Attempting database connection..." -ForegroundColor Yellow
    
    $testQuery = "SHOW TABLES;"
    $mysqlCommand = "mysql -h $dbHost -P $dbPort -u $dbUser -p$dbPassword $dbName -e `"$testQuery`""
    
    try {
        $result = Invoke-Expression $mysqlCommand 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ Successfully connected to database!" -ForegroundColor Green
            Write-Host "  Tables in database:" -ForegroundColor Cyan
            Write-Host $result
        }
        else {
            Write-Host "  ✗ Connection failed: $result" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "  ✗ Error executing MySQL command: $($_.Exception.Message)" -ForegroundColor Red
    }
}
else {
    Write-Host "[3/3] Skipping database connection test (prerequisites not met)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Connection Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Summary and Next Steps
Write-Host "SUMMARY:" -ForegroundColor Cyan
if ($portAccessible) {
    Write-Host "  ✓ Network connectivity OK" -ForegroundColor Green
}
else {
    Write-Host "  ✗ Network connectivity FAILED" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting steps:" -ForegroundColor Yellow
    Write-Host "  1. Verify server firewall allows port 3306" -ForegroundColor White
    Write-Host "  2. Check if MySQL is running on the server" -ForegroundColor White
    Write-Host "  3. Verify database server allows remote connections" -ForegroundColor White
    Write-Host "  4. Try using domain name: spatialcollective.co.ke" -ForegroundColor White
}

Write-Host ""
Write-Host "NEXT STEPS:" -ForegroundColor Cyan
Write-Host "  1. Run the database-setup.sql script to create tables" -ForegroundColor White
Write-Host "  2. Start the Spring Boot backend application" -ForegroundColor White
Write-Host "  3. Check application logs for connection status" -ForegroundColor White
Write-Host ""

# Offer to run setup script
if ($portAccessible -and $hasMysqlClient) {
    $runSetup = Read-Host "Do you want to run the database setup script now? (y/n)"
    if ($runSetup -eq 'y' -or $runSetup -eq 'Y') {
        $scriptPath = Join-Path $PSScriptRoot "database-setup.sql"
        
        if (Test-Path $scriptPath) {
            Write-Host "Running database-setup.sql..." -ForegroundColor Yellow
            $setupCommand = "mysql -h $dbHost -P $dbPort -u $dbUser -p$dbPassword $dbName < `"$scriptPath`""
            Invoke-Expression $setupCommand
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✓ Database setup completed successfully!" -ForegroundColor Green
            }
            else {
                Write-Host "✗ Database setup encountered errors" -ForegroundColor Red
            }
        }
        else {
            Write-Host "✗ database-setup.sql not found in current directory" -ForegroundColor Red
        }
    }
}

Write-Host ""
Read-Host "Press Enter to exit"
