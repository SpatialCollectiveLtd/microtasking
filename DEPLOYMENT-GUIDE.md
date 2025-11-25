# DPW Server Deployment Guide - Microtasking Application

**Server Status:** ✅ ONLINE  
**IP Address:** 102.220.23.109  
**SSH Port:** 22 (OPEN)  
**Admin Credentials:** admin / DPw!@2025  
**Root Credentials:** root / DPW!@2025!  

---

## Server Connection Test Results

```
✓ Server is REACHABLE (Ping: 489ms average)
✓ Port 22 (SSH) OPEN
✗ Port 80 (HTTP) CLOSED
✗ Port 443 (HTTPS) CLOSED  
✗ Port 3306 (MySQL) CLOSED
```

---

## Step 1: Connect to Server

### Option A: Windows Terminal (Recommended)
```bash
ssh admin@102.220.23.109
# Password: DPw!@2025
```

### Option B: PuTTY
1. Download PuTTY: https://www.putty.org/
2. Host Name: `102.220.23.109`
3. Port: `22`
4. Connection Type: SSH
5. Click "Open"
6. Login as: `admin`
7. Password: `DPw!@2025`

### Option C: MobaXterm
1. Session → SSH
2. Remote host: `102.220.23.109`
3. Username: `admin`
4. Port: `22`

---

## Step 2: Switch to Root User

After logging in as admin:
```bash
su root
# Password: DPW!@2025!
```

---

## Step 3: Check Server Environment

```bash
# Check OS version
cat /etc/os-release
# or
lsb_release -a

# Check available disk space
df -h

# Check memory
free -h

# Check if ports 80/443 are being used
netstat -tulpn | grep -E ':(80|443|3306) '

# Check installed software
which java
which mysql
which nginx
which apache2

# Check if MySQL/MariaDB is installed
mysql --version
systemctl status mysql
# or
systemctl status mariadb
```

---

## Step 4: System Requirements Check

The application needs:
- **Java 11+** (for Spring Boot backend)
- **MySQL or MariaDB** (database)
- **Nginx or Apache** (web server / reverse proxy)
- **Port 80** (HTTP) and **443** (HTTPS)
- **Domain name** (to point to 102.220.23.109)

---

## Step 5: Install Required Software

### Install Java 11
```bash
# Update package index
apt update

# Install OpenJDK 11
apt install -y openjdk-11-jdk

# Verify installation
java -version
javac -version
```

### Install MySQL
```bash
# Install MySQL Server
apt install -y mysql-server

# Secure MySQL installation
mysql_secure_installation

# Start MySQL service
systemctl start mysql
systemctl enable mysql

# Check status
systemctl status mysql
```

### Install Nginx (recommended over Apache)
```bash
# Install Nginx
apt install -y nginx

# Start Nginx
systemctl start nginx
systemctl enable nginx

# Check status
systemctl status nginx
```

---

## Step 6: Configure Firewall

```bash
# Check firewall status
ufw status

# If inactive, enable it
ufw enable

# Allow SSH (important!)
ufw allow 22/tcp

# Allow HTTP and HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# Allow MySQL (if external access needed)
ufw allow 3306/tcp

# Reload firewall
ufw reload

# Verify
ufw status numbered
```

---

## Step 7: Create MySQL Database

```bash
# Login to MySQL
mysql -u root -p

# In MySQL prompt:
CREATE DATABASE microtask_production;
CREATE USER 'microtask_user'@'localhost' IDENTIFIED BY 'YourStrongPasswordHere';
GRANT ALL PRIVILEGES ON microtask_production.* TO 'microtask_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

## Step 8: Upload Application Files

### Option A: Using SCP from Windows
```powershell
# From your local machine (PowerShell)
cd C:\Users\TECH\Desktop\microtasking\MicrotaskToolApi-master

# Upload backend JAR file
scp build/libs/microtask-tool-api-0.0.1-SNAPSHOT.jar admin@102.220.23.109:/home/admin/

# Upload frontend static files
scp -r src/main/resources/static/* admin@102.220.23.109:/home/admin/frontend/
```

### Option B: Using WinSCP
1. Download WinSCP: https://winscp.net/
2. Protocol: SFTP
3. Host: `102.220.23.109`
4. User: `admin`
5. Password: `DPw!@2025`
6. Upload files via drag-and-drop

### Option C: Using Git
```bash
# On the server
cd /opt
git clone <your-repo-url>
cd microtasking
```

---

## Step 9: Build Application on Server

```bash
# Navigate to project directory
cd /opt/microtasking/MicrotaskToolApi-master

# Make gradlew executable
chmod +x gradlew

# Build the application
./gradlew clean build

# The JAR file will be in: build/libs/
```

---

## Step 10: Configure Application

```bash
# Edit application.properties
nano src/main/resources/application.properties
```

Update these values:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/microtask_production
spring.datasource.username=microtask_user
spring.datasource.password=YourStrongPasswordHere

# Server Configuration
server.port=8080

# Google OAuth (use production domain)
spring.security.oauth2.client.registration.google.redirect-uri=https://yourdomain.com/login/oauth2/code/google
```

---

## Step 11: Create Systemd Service

```bash
# Create service file
nano /etc/systemd/system/microtask.service
```

Add this content:
```ini
[Unit]
Description=Microtask Tool API
After=syslog.target network.target

[Service]
User=admin
WorkingDirectory=/opt/microtasking/MicrotaskToolApi-master
ExecStart=/usr/bin/java -jar /opt/microtasking/MicrotaskToolApi-master/build/libs/microtask-tool-api-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start the service:
```bash
systemctl daemon-reload
systemctl enable microtask
systemctl start microtask
systemctl status microtask
```

---

## Step 12: Configure Nginx as Reverse Proxy

```bash
# Create Nginx config
nano /etc/nginx/sites-available/microtask
```

Add this configuration:
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    # Redirect HTTP to HTTPS (after SSL is configured)
    # return 301 https://$server_name$request_uri;

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Frontend static files
    location / {
        root /opt/microtasking/MicrotaskToolApi-master/src/main/resources/static;
        try_files $uri $uri/ /index.html;
    }
}
```

Enable the site:
```bash
ln -s /etc/nginx/sites-available/microtask /etc/nginx/sites-enabled/
nginx -t
systemctl reload nginx
```

---

## Step 13: Install SSL Certificate (Let's Encrypt)

```bash
# Install Certbot
apt install -y certbot python3-certbot-nginx

# Obtain certificate
certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Certbot will automatically configure HTTPS
```

---

## Step 14: Update Google OAuth Configuration

In Google Cloud Console:
1. Go to: https://console.cloud.google.com/apis/credentials
2. Edit OAuth Client ID: `34768107959-ad0p0iecdct89f4m401vsq4jo9u4afl5`
3. Add **Authorized JavaScript origins:**
   - `https://yourdomain.com`
4. Add **Authorized redirect URIs:**
   - `https://yourdomain.com`
5. Save and wait 1-2 minutes

---

## Step 15: Update Frontend Configuration

```bash
# Update API URL in compiled frontend
cd /opt/microtasking/MicrotaskToolApi-master/src/main/resources/static

# Find and replace localhost with production domain
find . -name "*.js" -type f -exec sed -i 's|http://localhost:8080|https://yourdomain.com/api|g' {} +
```

---

## Step 16: Create Admin User

```bash
# Use curl to create admin user
curl -X POST "http://localhost:8080/user/create-admin?id=118192627616311179805&fullName=Tech&email=tech@spatialcollective.com"
```

Or access from browser:
```
https://yourdomain.com/api/user/create-admin?id=118192627616311179805&fullName=Tech&email=tech@spatialcollective.com
```

---

## Step 17: Test Deployment

1. Visit: `https://yourdomain.com`
2. Click "Sign in with Google"
3. Authenticate with `tech@spatialcollective.com`
4. Should redirect to admin dashboard

---

## Troubleshooting Commands

### Check Backend Logs
```bash
# Real-time logs
journalctl -u microtask -f

# Last 100 lines
journalctl -u microtask -n 100

# Today's logs
journalctl -u microtask --since today
```

### Check Nginx Logs
```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### Restart Services
```bash
systemctl restart microtask
systemctl restart nginx
systemctl restart mysql
```

### Check Port Usage
```bash
netstat -tulpn | grep LISTEN
```

### Check Database
```bash
mysql -u microtask_user -p microtask_production

# In MySQL:
SHOW TABLES;
SELECT * FROM user WHERE email = 'tech@spatialcollective.com';
```

---

## Domain Setup

### Point Domain to Server

Update DNS records for your domain:
```
Type: A
Name: @ (or subdomain like 'app')
Value: 102.220.23.109
TTL: 3600
```

For www subdomain:
```
Type: A
Name: www
Value: 102.220.23.109
TTL: 3600
```

Wait 1-48 hours for DNS propagation.

---

## Security Checklist

- [ ] Firewall configured (ufw enabled)
- [ ] SSH key authentication enabled
- [ ] Password authentication disabled for SSH
- [ ] MySQL secured (mysql_secure_installation)
- [ ] Strong database passwords
- [ ] SSL certificate installed
- [ ] HTTP redirects to HTTPS
- [ ] Google OAuth production credentials
- [ ] Regular backups configured
- [ ] Fail2ban installed (optional but recommended)

---

## Backup Strategy

### Database Backup
```bash
# Create backup
mysqldump -u microtask_user -p microtask_production > /backups/db_$(date +%Y%m%d).sql

# Create automated daily backup (crontab)
crontab -e

# Add this line:
0 2 * * * mysqldump -u microtask_user -pYourPassword microtask_production > /backups/db_$(date +\%Y\%m\%d).sql
```

### Application Backup
```bash
# Backup uploaded files and configurations
tar -czf /backups/app_$(date +%Y%m%d).tar.gz /opt/microtasking
```

---

## Next Steps

1. **Connect to server** and verify SSH access
2. **Check installed software** (Java, MySQL, Nginx)
3. **Install missing components** as needed
4. **Configure firewall** to open ports 80/443
5. **Set up domain name** pointing to 102.220.23.109
6. **Upload and build application**
7. **Configure database**
8. **Set up reverse proxy**
9. **Install SSL certificate**
10. **Test complete authentication flow**

---

**Status:** Server is accessible. SSH port is open. Ready for deployment.
