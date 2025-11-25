# 🎯 DATABASE SETUP COMPLETE - SUMMARY

## ✅ What We've Configured

### 1. Database Connection (application.properties)
```properties
Host: 169.255.58.54 (spatialcollective.co.ke)
Database: spatialcoke_dpw_microtasking_prod
Username: spatialcoke_dpw_prod_user
Password: NtDcdgPoadgxT5
Port: 3306
```

### 2. Files Created
```
✓ database-setup.sql           - Creates all 5 tables with proper schema
✓ DATABASE-README.md           - Quick start guide
✓ DATABASE-SETUP-GUIDE.md      - Detailed troubleshooting guide
✓ test-db-connection.ps1       - PowerShell connection test
✓ test-db-connection.bat       - Windows batch connection test
✓ SETUP-SUMMARY.md             - This file
```

---

## 🚀 IMMEDIATE NEXT STEPS

### Step 1: Test Database Connection

Choose one method:

**Option A: Double-click** `test-db-connection.bat`

**Option B: Run PowerShell script**
```powershell
cd c:\Users\TECH\Desktop\microtasking
.\test-db-connection.ps1
```

**Option C: Manual test**
```bash
mysql -h 169.255.58.54 -u spatialcoke_dpw_prod_user -p spatialcoke_dpw_microtasking_prod
# Password: NtDcdgPoadgxT5
```

### Step 2: Run Database Setup Script

If connection works:

```bash
mysql -h 169.255.58.54 -u spatialcoke_dpw_prod_user -p spatialcoke_dpw_microtasking_prod < database-setup.sql
```

This creates:
- ✓ user table
- ✓ question table  
- ✓ task table
- ✓ image table
- ✓ answer table

### Step 3: Start Spring Boot Backend

```bash
cd MicrotaskToolApi-master
gradlew.bat bootRun
```

Watch for successful startup:
```
✓ HikariPool-1 - Start completed
✓ Started MicrotaskToolApiApplication
```

### Step 4: Verify in Browser

Open: http://localhost:8080/#/admin/sign-in

You should see the Google Sign-In page.

---

## 📊 Database Schema Created

```
user                      question                 task
┌─────────────────┐      ┌─────────────────┐      ┌──────────────────┐
│ id (PK)         │      │ id (PK)         │◄─────┤ question_id (FK) │
│ full_name       │      │ name            │      │ id (PK)          │
│ email           │      │ is_paused       │      │ worker_unique_id │
│ picture         │      │ created_at      │      │ phone_number     │
│ role            │      │ updated_at      │      │ start_date       │
└─────────────────┘      └─────────────────┘      │ progress         │
                                                   │ created_at       │
                                                   │ updated_at       │
                                                   └──────────────────┘

image                     answer
┌─────────────────┐      ┌──────────────────┐
│ id (PK)         │      │ id (PK)          │
│ url             │      │ image_id         │
│ created_at      │      │ url              │
│ question_id (FK)│◄─┐   │ worker_unique_id │
└─────────────────┘  │   │ answer           │
                     └───┤ question_id (FK) │
                         │ created_at       │
                         │ updated_at       │
                         └──────────────────┘
```

---

## 🔍 Quick Verification Queries

### After running setup script:

```sql
-- Check all tables exist
SHOW TABLES;

-- Count records (should be 0 initially)
SELECT 'user' as tbl, COUNT(*) FROM user
UNION ALL SELECT 'question', COUNT(*) FROM question
UNION ALL SELECT 'task', COUNT(*) FROM task
UNION ALL SELECT 'image', COUNT(*) FROM image
UNION ALL SELECT 'answer', COUNT(*) FROM answer;

-- Check table structures
DESCRIBE user;
DESCRIBE question;
DESCRIBE task;
DESCRIBE image;
DESCRIBE answer;
```

---

## ⚠️ Common Issues & Solutions

### Issue 1: "Can't connect to MySQL server"

**Cause:** Port 3306 blocked or MySQL not accepting remote connections

**Solution:**
1. Contact server admin to open port 3306
2. Verify MySQL bind-address is set to 0.0.0.0 (not 127.0.0.1)
3. Try using domain name instead of IP:
   - Edit `application.properties`
   - Uncomment the alternative URL line

### Issue 2: "Access denied"

**Cause:** User permissions not set correctly

**Solution:** Ask server admin to run:
```sql
GRANT ALL PRIVILEGES ON spatialcoke_dpw_microtasking_prod.* 
TO 'spatialcoke_dpw_prod_user'@'%';
FLUSH PRIVILEGES;
```

### Issue 3: "Unknown database"

**Cause:** Database doesn't exist

**Solution:**
```sql
CREATE DATABASE spatialcoke_dpw_microtasking_prod 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

---

## 🎯 After Database is Running

### Create First Admin User

**Method 1: Via Google Login (Recommended)**
1. Start backend: `gradlew.bat bootRun`
2. Open: http://localhost:8080/#/admin/sign-in
3. Click Google Sign-In
4. Login with your Google account
5. Check database - user created with role='Worker'
6. Update role:
   ```sql
   UPDATE user SET role='Admin' WHERE email='your@email.com';
   ```

**Method 2: Direct Database Insert (Testing)**
```sql
INSERT INTO user (id, full_name, email, role) 
VALUES ('test-admin', 'Test Admin', 'admin@test.com', 'Admin');
```

---

## 📝 Configuration Summary

### Application Properties (MicrotaskToolApi-master/src/main/resources/application.properties)

```properties
✓ Database URL updated to your server
✓ Username: spatialcoke_dpw_prod_user
✓ Password: NtDcdgPoadgxT5
✓ Hibernate ddl-auto: update (auto-creates tables)
✓ Port: 8080
✓ File upload: 10MB max per file, 40MB max request
✓ Logging configured for connection debugging
```

---

## 🔄 What Happens When You Start the App

1. **Spring Boot starts**
   - Loads application.properties
   - Connects to MySQL at 169.255.58.54

2. **Hibernate initializes**
   - Checks existing tables
   - Creates missing tables (if ddl-auto=update)
   - Updates schema if entities changed

3. **Server ready**
   - Listens on http://localhost:8080
   - API endpoints available
   - Frontend can connect

---

## 📋 Checklist Before Testing

- [ ] MySQL server is running on 169.255.58.54
- [ ] Port 3306 is accessible from your machine
- [ ] Database `spatialcoke_dpw_microtasking_prod` exists
- [ ] User `spatialcoke_dpw_prod_user` has permissions
- [ ] Ran `database-setup.sql` successfully
- [ ] All 5 tables created
- [ ] Spring Boot application starts without errors
- [ ] Can access http://localhost:8080/#/admin/sign-in
- [ ] At least one admin user exists

---

## 🚦 Status Check

Run this command to verify everything:

```bash
cd MicrotaskToolApi-master
gradlew.bat bootRun
```

### ✅ Success Looks Like:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.6.7)

INFO o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
INFO c.s.m.MicrotaskToolApiApplication        : Started MicrotaskToolApiApplication in 8.5 seconds
```

### ❌ Failure Looks Like:
```
Error creating bean with name 'dataSource'
Communications link failure
Access denied for user
```

If you see errors, check the troubleshooting section in DATABASE-SETUP-GUIDE.md

---

## 📞 Need Help?

1. Check detailed guide: `DATABASE-SETUP-GUIDE.md`
2. Run connection test: `test-db-connection.ps1`
3. Enable debug logging in application.properties:
   ```properties
   logging.level.org.hibernate.sql=DEBUG
   logging.level.org.springframework.web=DEBUG
   ```

---

## 🎉 Success Criteria

✅ Database connected  
✅ Tables created  
✅ Spring Boot running  
✅ Admin login page loads  
✅ Can sign in with Google  
✅ Admin user has role='Admin'  

**Once all checked, you're ready to:**
- Update frontend API URL
- Setup image hosting
- Create first question
- Test worker workflow

---

**Status:** 🟢 Database configuration complete - Ready to test!

**Next:** Start the backend and verify connection works.
