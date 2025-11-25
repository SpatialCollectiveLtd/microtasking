# Step 1: Check Server Environment
# This script checks what's installed on the DPW server

. .\deploy-helpers.ps1

Write-Host "=== CHECKING DPW SERVER ENVIRONMENT ===" -ForegroundColor Cyan
Write-Host "Server: $Global:ServerIP" -ForegroundColor White
Write-Host ""

# Note: Since we don't have automated SSH access yet, 
# we'll output the commands for you to run manually

Write-Host "=== COMMANDS TO RUN ON SERVER ===" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Login to server:" -ForegroundColor Cyan
Write-Host "   ssh admin@102.220.23.109" -ForegroundColor White
Write-Host "   Password: DPw!@2025" -ForegroundColor White
Write-Host ""

Write-Host "2. Check system information:" -ForegroundColor Cyan
Write-Host @"
# OS Version
cat /etc/os-release

# Disk space
df -h

# Memory
free -h

# CPU info
lscpu | grep -E 'Model name|CPU\(s\):'
"@ -ForegroundColor White

Write-Host ""
Write-Host "3. Check installed software:" -ForegroundColor Cyan
Write-Host @"
# Check Java
java -version 2>&1 || echo "Java not installed"
which java

# Check MySQL/MariaDB
mysql --version 2>&1 || echo "MySQL not installed"
systemctl status mysql 2>&1 || systemctl status mariadb 2>&1 || echo "MySQL service not found"

# Check Nginx
nginx -v 2>&1 || echo "Nginx not installed"
systemctl status nginx 2>&1 || echo "Nginx service not found"

# Check Apache
apache2 -v 2>&1 || httpd -v 2>&1 || echo "Apache not installed"

# Check Git
git --version 2>&1 || echo "Git not installed"

# Check if firewall is active
ufw status 2>&1 || iptables -L 2>&1 || echo "Firewall info not available"
"@ -ForegroundColor White

Write-Host ""
Write-Host "4. Check open ports:" -ForegroundColor Cyan
Write-Host @"
# List listening ports
netstat -tulpn | grep LISTEN || ss -tulpn | grep LISTEN

# Check specific ports
netstat -tulpn | grep -E ':(80|443|3306|8080) ' || ss -tulpn | grep -E ':(80|443|3306|8080) '
"@ -ForegroundColor White

Write-Host ""
Write-Host "5. Switch to root (if needed):" -ForegroundColor Cyan
Write-Host "   su root" -ForegroundColor White
Write-Host "   Password: DPW!@2025!" -ForegroundColor White
Write-Host ""

Write-Host "=== SAVE OUTPUTS ===" -ForegroundColor Yellow
Write-Host ""
Write-Host "After running the commands above, save the output and share it with me." -ForegroundColor White
Write-Host "This will help me create the next deployment scripts." -ForegroundColor White
Write-Host ""

Write-Host "Alternative: Copy and paste the output here, or save to a file:" -ForegroundColor Cyan
Write-Host "   # On the server, save to file:" -ForegroundColor White
Write-Host "   bash check-environment.sh > /tmp/server-info.txt" -ForegroundColor White
Write-Host "   # Then download it:" -ForegroundColor White
Write-Host "   scp admin@102.220.23.109:/tmp/server-info.txt ." -ForegroundColor White
Write-Host ""

# Create a bash script file that can be uploaded to server
$bashScript = @'
#!/bin/bash
# Server Environment Check Script
# Run this on the DPW server and save the output

echo "========================================="
echo "DPW SERVER ENVIRONMENT CHECK"
echo "Date: $(date)"
echo "========================================="
echo ""

echo "--- SYSTEM INFORMATION ---"
echo "OS Version:"
cat /etc/os-release 2>&1
echo ""

echo "Hostname:"
hostname
echo ""

echo "Disk Space:"
df -h
echo ""

echo "Memory:"
free -h
echo ""

echo "CPU Info:"
lscpu | grep -E 'Model name|CPU\(s\):'
echo ""

echo "--- INSTALLED SOFTWARE ---"
echo "Java:"
java -version 2>&1 || echo "NOT INSTALLED"
which java 2>&1
echo ""

echo "MySQL/MariaDB:"
mysql --version 2>&1 || echo "NOT INSTALLED"
systemctl status mysql 2>&1 | head -5 || systemctl status mariadb 2>&1 | head -5 || echo "SERVICE NOT FOUND"
echo ""

echo "Nginx:"
nginx -v 2>&1 || echo "NOT INSTALLED"
systemctl status nginx 2>&1 | head -5 || echo "SERVICE NOT FOUND"
echo ""

echo "Apache:"
apache2 -v 2>&1 || httpd -v 2>&1 || echo "NOT INSTALLED"
echo ""

echo "Git:"
git --version 2>&1 || echo "NOT INSTALLED"
echo ""

echo "Curl:"
curl --version 2>&1 | head -1 || echo "NOT INSTALLED"
echo ""

echo "--- NETWORK INFORMATION ---"
echo "Open Ports:"
netstat -tulpn 2>&1 | grep LISTEN || ss -tulpn 2>&1 | grep LISTEN
echo ""

echo "Firewall Status:"
ufw status 2>&1 || iptables -L 2>&1 | head -10 || echo "Firewall info not available"
echo ""

echo "--- RUNNING SERVICES ---"
systemctl list-units --type=service --state=running 2>&1 | head -20
echo ""

echo "========================================="
echo "ENVIRONMENT CHECK COMPLETE"
echo "========================================="
'@

$bashScript | Out-File -FilePath "check-environment.sh" -Encoding UTF8
Write-Host "Created: check-environment.sh" -ForegroundColor Green
Write-Host ""
Write-Host "Upload this script to the server and run it:" -ForegroundColor Yellow
Write-Host "   scp check-environment.sh admin@102.220.23.109:~/" -ForegroundColor White
Write-Host "   ssh admin@102.220.23.109" -ForegroundColor White
Write-Host "   chmod +x check-environment.sh" -ForegroundColor White
Write-Host "   ./check-environment.sh > server-info.txt" -ForegroundColor White
Write-Host "   cat server-info.txt" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to continue"
