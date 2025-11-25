# Database Credentials and Configuration

## Production Database (Bluehost MySQL)

### Connection Details
```properties
Database Host: spatialcollective.com
Port: 3306
Database Name: bgprxgmy_microtask
Username: bgprxgmy_microtask
Password: r]VOZwCCLIDX
```

### JDBC Connection String
```
jdbc:mysql://spatialcollective.com:3306/bgprxgmy_microtask
```

### Spring Boot Configuration (application.properties)

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://spatialcollective.com:3306/bgprxgmy_microtask
spring.datasource.username=bgprxgmy_microtask
spring.datasource.password=r]VOZwCCLIDX
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# Server Configuration
server.port=8080

# OAuth Configuration
spring.security.oauth2.client.registration.google.client-id=34768107959-ad0p0iecdct89f4m401vsq4jo9u4afl5.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=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
```

---

## Database Schema

The database already exists with the following tables:

### 1. **user** table
- `id` VARCHAR(255) PRIMARY KEY - Google OAuth 'sub' claim
- `email` VARCHAR(255) - User email
- `full_name` VARCHAR(255) - Display name
- `picture` VARCHAR(512) - Profile picture URL
- `role` VARCHAR(50) - Either 'Admin' or 'Worker'
- `created_date` DATETIME
- `last_modified_date` DATETIME

### 2. **question** table
- `id` VARCHAR(255) PRIMARY KEY
- `name` VARCHAR(255) - Question text
- `created_date` DATETIME
- `last_modified_date` DATETIME

### 3. **task** table
- `id` VARCHAR(255) PRIMARY KEY
- `question_id` VARCHAR(255) - Foreign key to question
- `status` VARCHAR(50) - Task status
- `created_date` DATETIME
- `last_modified_date` DATETIME

### 4. **image** table (actually named 'link' in database)
- `id` VARCHAR(255) PRIMARY KEY
- `url` VARCHAR(2048) - Image URL
- `task_id` VARCHAR(255) - Foreign key to task
- `created_date` DATETIME
- `last_modified_date` DATETIME

### 5. **answer** table
- `id` VARCHAR(255) PRIMARY KEY
- `user_id` VARCHAR(255) - Foreign key to user
- `link_id` VARCHAR(255) - Foreign key to link/image
- `answer_value` TEXT - User's answer
- `created_date` DATETIME
- `last_modified_date` DATETIME

---

## Admin User

### Current Admin Account
```sql
ID: 118192627616311179805
Email: tech@spatialcollective.com
Full Name: Tech
Role: Admin
```

**IMPORTANT:** The admin user ID MUST match the Google OAuth 'sub' claim (118192627616311179805) from the Google account tech@spatialcollective.com.

### To Verify Admin User Exists
```sql
SELECT * FROM user WHERE email = 'tech@spatialcollective.com';
```

### To Create Admin User (if needed)
```sql
INSERT INTO user (id, full_name, email, picture, role, created_date, last_modified_date)
VALUES (
    '118192627616311179805',
    'Tech',
    'tech@spatialcollective.com',
    '',
    'Admin',
    NOW(),
    NOW()
);
```

---

## Google OAuth Configuration

### OAuth Credentials
```
Client ID: 34768107959-ad0p0iecdct89f4m401vsq4jo9u4afl5.apps.googleusercontent.com
Client Secret: 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
```

### Authorized JavaScript Origins
```
http://localhost:8080
http://localhost:3000
```

### Authorized Redirect URIs
```
http://localhost:8080
```

**Note:** For production deployment, you'll need to add your production domain to the authorized origins and redirect URIs.

---

## MySQL Direct Access

### Using MySQL Command Line
```bash
mysql -h spatialcollective.com -P 3306 -u bgprxgmy_microtask -p
# Password: r]VOZwCCLIDX

# Then select database
USE bgprxgmy_microtask;
```

### Using MySQL Workbench
```
Connection Name: Microtask Production
Hostname: spatialcollective.com
Port: 3306
Username: bgprxgmy_microtask
Password: r]VOZwCCLIDX
Default Schema: bgprxgmy_microtask
```

---

## Important Notes

### ⚠️ DO NOT RUN SCHEMA CREATION
The database and all tables **already exist**. Hibernate is configured to use `spring.jpa.hibernate.ddl-auto=update` which will:
- ✅ Keep existing tables
- ✅ Update schema if entities change
- ❌ NOT drop or recreate tables

### ✅ Database is Ready
- All tables created ✓
- Admin user exists ✓
- Schema matches entities ✓
- Connection tested ✓

### 🔒 Security
- This database is on shared hosting (Bluehost)
- Use secure connections in production
- Keep credentials confidential
- Don't commit passwords to public repositories

---

## Testing Database Connection

### From Spring Boot Application
The application will automatically connect using the credentials in `application.properties`.

Check logs for:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

### Quick Test Query
```sql
-- Check if tables exist
SHOW TABLES;

-- Check admin user
SELECT id, email, full_name, role FROM user WHERE role = 'Admin';

-- Count records
SELECT 
    (SELECT COUNT(*) FROM user) as users,
    (SELECT COUNT(*) FROM question) as questions,
    (SELECT COUNT(*) FROM task) as tasks,
    (SELECT COUNT(*) FROM link) as images,
    (SELECT COUNT(*) FROM answer) as answers;
```

---

## Server Deployment Configuration

When deploying to the production server (102.210.149.40), use the **same database credentials** above. The server will connect remotely to the Bluehost MySQL database.

### No need to install MySQL on the server
The application connects to the remote Bluehost database, so you don't need to set up a local MySQL instance on the deployment server.

---

## Troubleshooting

### Connection Timeout
If you get connection timeouts:
- Check firewall allows outbound connections to port 3306
- Verify the database host is accessible: `ping spatialcollective.com`
- Test MySQL port: `telnet spatialcollective.com 3306`

### Authentication Failed
- Double-check username: `bgprxgmy_microtask`
- Double-check password: `r]VOZwCCLIDX`
- Ensure no extra spaces in credentials

### Table Not Found
- Tables are created by Hibernate automatically with `ddl-auto=update`
- If tables don't exist, start the Spring Boot application once to create them
- Check database name is correct: `bgprxgmy_microtask`

---

## Quick Reference

**One-liner for application.properties:**
```properties
spring.datasource.url=jdbc:mysql://spatialcollective.com:3306/bgprxgmy_microtask
spring.datasource.username=bgprxgmy_microtask
spring.datasource.password=r]VOZwCCLIDX
```

**Admin User ID (critical for OAuth):**
```
118192627616311179805
```

**Database is already set up - just connect and run!** 🚀
