# 🔧 FOR SERVER ADMINISTRATOR - URGENT DATABASE SETUP

## Current Credentials

**Database:** spatialcoke_microtask  
**Username:** spatialcoke_microtask  
**Password:** vVMED6Eq2YZExZDt8rfW  
**Host:** spatialcollective.co.ke (IP: 169.255.58.54)

## Connection Test Results

✅ **Port 3306 is accessible** - Network connectivity confirmed  
❌ **MySQL connection times out** - Database/configuration issue

**Error:** `java.net.ConnectException: Connection timed out`

This means MySQL is not responding to connection attempts even though the port is open.

---

## Required Actions (Run on MySQL Server)

### Step 1: Verify MySQL is Running

```bash
# Check MySQL status
sudo systemctl status mysql
# or
sudo systemctl status mariadb

# If not running, start it
sudo systemctl start mysql
```

### Step 2: Create Database

Login to MySQL as root:
```bash
mysql -u root -p
```

Then run:
```sql
-- Create the database
CREATE DATABASE IF NOT EXISTS spatialcoke_microtask
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Verify it was created
SHOW DATABASES LIKE 'spatialcoke%';
```

### Step 3: Grant Remote Access Permissions

```sql
-- Grant all privileges to the user
GRANT ALL PRIVILEGES ON spatialcoke_microtask.* 
TO 'spatialcoke_microtask'@'%' 
IDENTIFIED BY 'vVMED6Eq2YZExZDt8rfW';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify permissions
SHOW GRANTS FOR 'spatialcoke_microtask'@'%';
```

**Expected output:**
```
+-------------------------------------------------------------------+
| Grants for spatialcoke_microtask@%                               |
+-------------------------------------------------------------------+
| GRANT USAGE ON *.* TO `spatialcoke_microtask`@`%`               |
| GRANT ALL PRIVILEGES ON `spatialcoke_microtask`.*               |
| TO `spatialcoke_microtask`@`%`                                   |
+-------------------------------------------------------------------+
```

### Step 4: Enable Remote Connections

Edit MySQL configuration:

**For MySQL:**
```bash
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

**For MariaDB:**
```bash
sudo nano /etc/mysql/mariadb.conf.d/50-server.cnf
```

Find and change:
```ini
# FROM:
bind-address = 127.0.0.1

# TO:
bind-address = 0.0.0.0
```

Save and restart MySQL:
```bash
sudo systemctl restart mysql
```

### Step 5: Verify Port is Open

```bash
# Check if MySQL is listening on all interfaces
sudo netstat -tlnp | grep 3306

# Should show:
# tcp 0 0 0.0.0.0:3306  0.0.0.0:*  LISTEN  12345/mysqld
```

### Step 6: Test Connection from Server

```bash
# Test connection locally
mysql -h 169.255.58.54 -u spatialcoke_microtask -p spatialcoke_microtask
# Password: vVMED6Eq2YZExZDt8rfW

# If successful, run:
SHOW TABLES;
# Should show empty set (no tables yet)
```

---

## Quick Copy-Paste Script for Server Admin

```bash
#!/bin/bash
# Run this on the MySQL server

echo "Creating database and user permissions for Microtasking App..."

mysql -u root -p <<EOF
-- Create database
CREATE DATABASE IF NOT EXISTS spatialcoke_microtask
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Grant permissions
GRANT ALL PRIVILEGES ON spatialcoke_microtask.* 
TO 'spatialcoke_microtask'@'%' 
IDENTIFIED BY 'vVMED6Eq2YZExZDt8rfW';

FLUSH PRIVILEGES;

-- Show status
SHOW DATABASES LIKE 'spatialcoke%';
SHOW GRANTS FOR 'spatialcoke_microtask'@'%';
EOF

echo ""
echo "Checking MySQL bind address..."
grep "bind-address" /etc/mysql/mysql.conf.d/mysqld.cnf || grep "bind-address" /etc/mysql/mariadb.conf.d/50-server.cnf

echo ""
echo "If bind-address is 127.0.0.1, change it to 0.0.0.0 and restart MySQL:"
echo "sudo systemctl restart mysql"
```

---

## Troubleshooting

### Issue: "Access denied for root"
```bash
# Reset root password
sudo mysql_secure_installation
```

### Issue: "bind-address not found"
```bash
# Add it manually
echo "bind-address = 0.0.0.0" | sudo tee -a /etc/mysql/mysql.conf.d/mysqld.cnf
sudo systemctl restart mysql
```

### Issue: Firewall blocking
```bash
# Ubuntu/Debian
sudo ufw allow 3306/tcp

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --reload
```

---

## Verification Checklist

After completing above steps, verify:

- [ ] Database `spatialcoke_microtask` exists
- [ ] User `spatialcoke_microtask` has permissions
- [ ] MySQL bind-address is 0.0.0.0
- [ ] Port 3306 is open in firewall
- [ ] MySQL service is running
- [ ] Can connect locally: `mysql -h 169.255.58.54 -u spatialcoke_microtask -p`

---

## What Happens Next

Once you complete these steps:
1. The application developer will run the setup script again
2. Database tables will be auto-created by Hibernate
3. Application will start successfully
4. Admin can begin creating microtask questions

---

## Contact

If issues persist, provide this information:

```bash
# MySQL version
mysql --version

# Error logs
sudo tail -50 /var/log/mysql/error.log

# User permissions
mysql -u root -p -e "SELECT host, user FROM mysql.user WHERE user='spatialcoke_microtask';"

# Database list
mysql -u root -p -e "SHOW DATABASES;"
```

---

**Priority: HIGH** - Application cannot start until database connection works.
