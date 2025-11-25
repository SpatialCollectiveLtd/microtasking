# Database Setup Guide

## Connection Details

**Database Host:** 169.255.58.54 (spatialcollective.co.ke)  
**Database Name:** spatialcoke_dpw_microtasking_prod  
**Username:** spatialcoke_dpw_prod_user  
**Password:** NtDcdgPoadgxT5  
**Port:** 3306

---

## Step 1: Test Database Connection

### Option A: Using MySQL Client (Command Line)

```bash
mysql -h 169.255.58.54 -u spatialcoke_dpw_prod_user -p spatialcoke_dpw_microtasking_prod
# Enter password when prompted: NtDcdgPoadgxT5
```

### Option B: Using MySQL Workbench (GUI)

1. Open MySQL Workbench
2. Create new connection:
   - **Connection Name:** Microtasking Production
   - **Hostname:** 169.255.58.54
   - **Port:** 3306
   - **Username:** spatialcoke_dpw_prod_user
   - **Password:** Store in keychain/vault
   - **Default Schema:** spatialcoke_dpw_microtasking_prod

### Option C: Using phpMyAdmin (Web Interface)

If your server has phpMyAdmin installed:
- URL: http://spatialcollective.co.ke/phpmyadmin (or similar)
- Use the credentials above

---

## Step 2: Run Database Setup Script

### Method 1: Command Line

```bash
# From the project directory
mysql -h 169.255.58.54 -u spatialcoke_dpw_prod_user -p spatialcoke_dpw_microtasking_prod < database-setup.sql
```

### Method 2: MySQL Workbench

1. Connect to database
2. File → Open SQL Script → Select `database-setup.sql`
3. Execute (lightning bolt icon or Ctrl+Shift+Enter)

### Method 3: Copy-Paste

1. Open `database-setup.sql`
2. Copy entire contents
3. Paste into MySQL client or phpMyAdmin
4. Execute

---

## Step 3: Verify Tables Created

Run this query:

```sql
USE spatialcoke_dpw_microtasking_prod;
SHOW TABLES;
```

You should see:
- answer
- image
- question
- task
- user

Check table structure:

```sql
DESCRIBE user;
DESCRIBE question;
DESCRIBE task;
DESCRIBE image;
DESCRIBE answer;
```

---

## Step 4: Test Spring Boot Connection

### Start the Backend Application

```bash
cd MicrotaskToolApi-master
./gradlew bootRun
```

Watch for these log messages:

✅ **SUCCESS:**
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Hibernate: create table...
```

❌ **FAILURE:**
```
Communications link failure
Access denied for user
Unknown database
```

### Common Connection Issues

#### Issue 1: Communications Link Failure
```
Caused by: java.net.ConnectException: Connection refused
```

**Solutions:**
1. Check if MySQL port 3306 is open on firewall
2. Verify server allows remote connections
3. Try using domain name instead of IP (uncomment alternative URL in application.properties)

```properties
# Try this if IP doesn't work:
spring.datasource.url=jdbc:mysql://spatialcollective.co.ke:3306/spatialcoke_dpw_microtasking_prod?serverTimezone=UTC&useLegacyDatetimeCode=false&cachePrepStmts=true&useServerPrepStmts=true&rewriteBatchedStatements=true&useSSL=false&allowPublicKeyRetrieval=true
```

#### Issue 2: Access Denied
```
Access denied for user 'spatialcoke_dpw_prod_user'@'xxx.xxx.xxx.xxx'
```

**Solutions:**
1. Verify username/password are correct
2. Check user has remote access permissions:

```sql
-- Run on server as root
SELECT host, user FROM mysql.user WHERE user='spatialcoke_dpw_prod_user';

-- Grant remote access if needed
GRANT ALL PRIVILEGES ON spatialcoke_dpw_microtasking_prod.* 
TO 'spatialcoke_dpw_prod_user'@'%' 
IDENTIFIED BY 'NtDcdgPoadgxT5';
FLUSH PRIVILEGES;
```

#### Issue 3: Unknown Database
```
Unknown database 'spatialcoke_dpw_microtasking_prod'
```

**Solution:**
Database doesn't exist. Create it:

```sql
CREATE DATABASE spatialcoke_dpw_microtasking_prod 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

#### Issue 4: SSL Connection Error
```
SSL connection error
```

**Solution:**
Already handled by `useSSL=false` in connection string

---

## Step 5: Create First Admin User

### Method 1: Direct Database Insert (Temporary)

For initial testing only:

```sql
INSERT INTO user (id, full_name, email, picture, role) 
VALUES ('test-admin-001', 'Test Admin', 'admin@spatialcollective.co.ke', '', 'Admin');
```

### Method 2: Google OAuth (Production Method)

1. Start the application
2. Navigate to: http://localhost:8080/#/admin/sign-in
3. Click "Sign in with Google"
4. Use Google account you want as admin
5. User will be auto-created with role='Worker'
6. Manually update role to 'Admin':

```sql
UPDATE user 
SET role = 'Admin' 
WHERE email = 'youremail@gmail.com';
```

---

## Step 6: Verify Everything Works

### Test Queries

```sql
-- Count records
SELECT 'user' AS table_name, COUNT(*) AS count FROM user
UNION ALL SELECT 'question', COUNT(*) FROM question
UNION ALL SELECT 'task', COUNT(*) FROM task
UNION ALL SELECT 'image', COUNT(*) FROM image
UNION ALL SELECT 'answer', COUNT(*) FROM answer;

-- Check admin users
SELECT id, full_name, email, role FROM user WHERE role='Admin';

-- View database size
SELECT 
    table_name AS 'Table',
    table_rows AS 'Rows',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES 
WHERE table_schema = 'spatialcoke_dpw_microtasking_prod'
ORDER BY (data_length + index_length) DESC;
```

---

## Troubleshooting Checklist

- [ ] Database exists and is accessible
- [ ] User has correct permissions (SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER)
- [ ] Port 3306 is open in firewall
- [ ] MySQL server allows remote connections (bind-address = 0.0.0.0 in my.cnf)
- [ ] Connection string in application.properties is correct
- [ ] All 5 tables created successfully
- [ ] Spring Boot application starts without errors
- [ ] Can access admin login page
- [ ] At least one admin user exists

---

## Next Steps

Once database is set up and Spring Boot connects successfully:

1. ✅ Update frontend API URL to point to your server
2. ✅ Set up image hosting directory on server
3. ✅ Prepare CSV files for first question
4. ✅ Test complete workflow (create question → worker login → submit answers)

---

## Important Security Notes

⚠️ **AFTER SETUP:**
1. Change default passwords
2. Use environment variables for credentials (not hardcoded)
3. Enable SSL for MySQL connections in production
4. Restrict database user to specific IP ranges if possible
5. Regular backups (daily recommended)

---

## Need Help?

Common commands:

```bash
# Check if MySQL port is accessible
telnet 169.255.58.54 3306

# Test connection with curl (from Spring Boot app)
curl -v telnet://169.255.58.54:3306

# View Spring Boot logs
cd MicrotaskToolApi-master
./gradlew bootRun --debug

# Check Hibernate SQL queries
# Uncomment in application.properties:
# logging.level.org.hibernate.sql=DEBUG
```
