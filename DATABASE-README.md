# 🗄️ DATABASE SETUP - QUICK START GUIDE

## ✅ What We've Done

1. **Updated database credentials** in `application.properties`
2. **Created SQL setup script** (`database-setup.sql`)
3. **Created test scripts** for connection verification

---

## 🚀 Quick Setup (Choose One Method)

### Method 1: Using Windows Batch File (Easiest)

```bash
# Just double-click this file:
test-db-connection.bat

# Or run from command prompt:
cd c:\Users\TECH\Desktop\microtasking
test-db-connection.bat
```

### Method 2: Using PowerShell (Recommended)

```powershell
cd c:\Users\TECH\Desktop\microtasking
.\test-db-connection.ps1
```

### Method 3: Manual MySQL Command

```bash
# Test connection
mysql -h 169.255.58.54 -u spatialcoke_dpw_prod_user -p spatialcoke_dpw_microtasking_prod
# Password: NtDcdgPoadgxT5

# Run setup script
mysql -h 169.255.58.54 -u spatialcoke_dpw_prod_user -p spatialcoke_dpw_microtasking_prod < database-setup.sql
```

---

## 📋 What Gets Created

The `database-setup.sql` script creates **5 tables**:

| Table | Purpose | Key Fields |
|-------|---------|-----------|
| **user** | Admin users (Google OAuth) | id, email, role |
| **question** | Microtask questions/projects | id, name, is_paused |
| **task** | Worker assignments | id, phone_number, progress |
| **image** | Images to annotate | id, url, question_id |
| **answer** | Worker responses | id, answer, worker_unique_id |

---

## 🔍 Verify Setup

### 1. Check Tables Created

```sql
USE spatialcoke_dpw_microtasking_prod;
SHOW TABLES;
```

Expected output:
```
+--------------------------------------------+
| Tables_in_spatialcoke_dpw_microtasking_prod |
+--------------------------------------------+
| answer                                      |
| image                                       |
| question                                    |
| task                                        |
| user                                        |
+--------------------------------------------+
```

### 2. Verify Table Structure

```sql
DESCRIBE user;
DESCRIBE question;
```

### 3. Test Spring Boot Connection

```bash
cd MicrotaskToolApi-master
gradlew.bat bootRun
```

Look for this in logs:
```
✓ HikariPool-1 - Starting...
✓ HikariPool-1 - Start completed.
✓ Started MicrotaskToolApiApplication in X.XXX seconds
```

---

## 🔧 Troubleshooting

### ❌ "Communications link failure"

**Problem:** Can't connect to MySQL server

**Solutions:**
1. Check if port 3306 is open:
   ```bash
   telnet 169.255.58.54 3306
   ```

2. Try using domain name instead:
   - Edit `application.properties`
   - Uncomment the alternative URL line with `spatialcollective.co.ke`

3. Contact server admin to:
   - Open port 3306 in firewall
   - Enable remote MySQL connections
   - Check MySQL is running

### ❌ "Access denied for user"

**Problem:** Wrong credentials or permissions

**Solutions:**
1. Verify credentials are correct
2. Ask server admin to grant permissions:
   ```sql
   GRANT ALL PRIVILEGES ON spatialcoke_dpw_microtasking_prod.* 
   TO 'spatialcoke_dpw_prod_user'@'%' 
   IDENTIFIED BY 'NtDcdgPoadgxT5';
   FLUSH PRIVILEGES;
   ```

### ❌ "Unknown database"

**Problem:** Database doesn't exist

**Solution:**
Create it first:
```sql
CREATE DATABASE spatialcoke_dpw_microtasking_prod 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

---

## 🎯 Next Steps After Database Setup

1. **Create First Admin User**
   - Start the backend app
   - Go to `http://localhost:8080/#/admin/sign-in`
   - Sign in with Google
   - Update role in database:
     ```sql
     UPDATE user SET role='Admin' WHERE email='youremail@gmail.com';
     ```

2. **Update Frontend API URL**
   - Will do this in next step

3. **Setup Image Hosting**
   - Will configure server directory for GoPro images

4. **Test Complete Workflow**
   - Create question
   - Upload images
   - Test worker login
   - Submit answers

---

## 📊 Database Monitoring Queries

### Count Records
```sql
SELECT 'users' as table_name, COUNT(*) as count FROM user
UNION ALL SELECT 'questions', COUNT(*) FROM question
UNION ALL SELECT 'tasks', COUNT(*) FROM task
UNION ALL SELECT 'images', COUNT(*) FROM image
UNION ALL SELECT 'answers', COUNT(*) FROM answer;
```

### Database Size
```sql
SELECT 
    table_name,
    ROUND((data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.TABLES 
WHERE table_schema = 'spatialcoke_dpw_microtasking_prod'
ORDER BY (data_length + index_length) DESC;
```

### Recent Activity
```sql
-- Recent questions
SELECT id, name, created_at, is_paused 
FROM question 
ORDER BY created_at DESC 
LIMIT 10;

-- Recent answers
SELECT COUNT(*) as total_answers, 
       DATE(created_at) as date
FROM answer 
GROUP BY DATE(created_at)
ORDER BY date DESC
LIMIT 7;
```

---

## 🔐 Security Notes

⚠️ **IMPORTANT:**
- Database credentials are currently hardcoded
- For production, use environment variables
- Enable SSL for MySQL connections
- Regular backups (daily recommended)
- Restrict database access by IP if possible

---

## 📞 Need Help?

Check the detailed guide: `DATABASE-SETUP-GUIDE.md`

Common issues and solutions are documented there.

---

## ✨ Files Created

```
microtasking/
├── database-setup.sql              # SQL script to create tables
├── DATABASE-SETUP-GUIDE.md         # Detailed setup guide
├── DATABASE-README.md              # This file (quick reference)
├── test-db-connection.ps1          # PowerShell test script
└── test-db-connection.bat          # Windows batch test script
```

---

**Status:** ✅ Database configuration complete - Ready to run setup script!
