# Collaborative Deployment Workflow

## How We'll Work Together

Since I can't directly access the server, we'll use a **hybrid approach**:

1. **I create scripts** with the exact commands needed
2. **You run the scripts** or copy-paste the output back to me
3. **I analyze the results** and create the next steps
4. **We iterate** until deployment is complete

---

## Three Ways to Work

### Option 1: Interactive SSH (Manual - Best for Learning)
```powershell
.\connect-to-server.ps1
```
- Opens SSH session
- You run commands I provide
- Copy/paste output back to me
- Good for: Understanding what's happening

### Option 2: Run Scripts on Server (Semi-Automated)
```powershell
.\deploy-step1-check-environment.ps1
```
- I create bash scripts
- You upload and run them on server
- Share the output with me
- Good for: Faster execution

### Option 3: Screen Sharing (Most Efficient)
- Share your screen via Teams/Zoom/Discord
- I tell you exactly what to type
- We see results together in real-time
- Good for: Quick deployment

---

## Current Status: Step 1 - Environment Check

**What we need to know:**
- What OS is running? (Ubuntu, CentOS, Debian?)
- What's already installed? (Java, MySQL, Nginx?)
- What ports are open?
- How much disk space is available?

**How to get this info:**

### Quick Method (5 minutes):
```powershell
# Run this script
.\connect-to-server.ps1

# Once logged in, run these commands one by one:
cat /etc/os-release
df -h
free -h
java -version
mysql --version
nginx -v
systemctl status mysql
ufw status
```

Copy all the output and share it with me.

### Automated Method (3 minutes):
```powershell
# 1. Run the environment check script
.\deploy-step1-check-environment.ps1

# 2. It will create check-environment.sh
# 3. Upload it to server:
scp check-environment.sh admin@102.220.23.109:~/

# 4. Connect and run it:
ssh admin@102.220.23.109
chmod +x check-environment.sh
./check-environment.sh > server-info.txt
cat server-info.txt

# 5. Copy the output and share it with me
```

---

## What Happens Next?

Once I see the server environment info, I'll create:

### Step 2: Install Missing Software
- Script to install Java 11 (if needed)
- Script to install MySQL (if needed)
- Script to install Nginx (if needed)

### Step 3: Configure Firewall
- Script to open ports 80, 443
- Configure UFW or iptables

### Step 4: Set Up Database
- Create database and user
- Configure MySQL for remote access (if needed)

### Step 5: Upload Application
- Build the application locally
- Upload JAR file to server
- Upload static files

### Step 6: Configure Application
- Update application.properties for production
- Set up systemd service
- Configure Nginx reverse proxy

### Step 7: SSL Certificate
- Install Certbot
- Get Let's Encrypt certificate
- Configure HTTPS

### Step 8: Testing
- Create admin user on production
- Test OAuth login
- Verify all features work

---

## Example Workflow

**You:**
```
I ran the environment check. Here's the output:
[paste server info]
```

**Me:**
```
Thanks! I see you have:
- Ubuntu 22.04 ✓
- Java 11 installed ✓
- MySQL not installed ✗
- Nginx not installed ✗

Here's the installation script...
[create install-mysql-nginx.sh]
```

**You:**
```
[run the script]
Here's what happened:
[paste output]
```

**Me:**
```
Perfect! MySQL and Nginx are now installed.
Next, let's configure the firewall...
[create configure-firewall.sh]
```

And so on until deployment is complete!

---

## Tips for Efficient Collaboration

### When Sharing Output:
✅ **DO:** Copy the full output, including error messages
✅ **DO:** Mention if something failed or timed out
✅ **DO:** Let me know if you're prompted for input
❌ **DON'T:** Summarize - I need exact output
❌ **DON'T:** Skip error messages

### When Running Commands:
✅ **DO:** Copy-paste exactly as I provide
✅ **DO:** Run commands one at a time if unsure
✅ **DO:** Ask if something looks wrong
❌ **DON'T:** Modify commands without telling me
❌ **DON'T:** Skip steps

---

## Available Scripts

### Already Created:
- `connect-to-server.ps1` - Opens SSH session
- `deploy-step1-check-environment.ps1` - Checks server environment
- `check-environment.sh` - Bash script for server environment check

### Will Create Based on Your Environment:
- `install-software.sh` - Install Java, MySQL, Nginx
- `configure-firewall.sh` - Open necessary ports
- `setup-database.sh` - Create database and user
- `deploy-application.sh` - Upload and configure app
- `setup-nginx.sh` - Configure reverse proxy
- `install-ssl.sh` - Set up HTTPS
- `create-systemd-service.sh` - Auto-start application

---

## Emergency Commands

### If Something Goes Wrong:

**Stop Application:**
```bash
sudo systemctl stop microtask
```

**Restart MySQL:**
```bash
sudo systemctl restart mysql
```

**Restart Nginx:**
```bash
sudo systemctl restart nginx
```

**Check Logs:**
```bash
# Application logs
journalctl -u microtask -n 50

# Nginx error log
tail -50 /var/log/nginx/error.log

# MySQL log
tail -50 /var/log/mysql/error.log
```

**Rollback Firewall:**
```bash
sudo ufw disable
```

---

## Security Note

The scripts I create will:
- ✓ Use the credentials you provided
- ✓ Follow security best practices
- ✓ Enable firewall after configuration
- ✓ Use strong passwords for database
- ✓ Set up HTTPS with Let's Encrypt

Make sure to:
- ⚠️ Change default passwords after deployment
- ⚠️ Keep your server credentials private
- ⚠️ Take backups before making changes

---

## Let's Start!

Run one of these to begin:

**Quick check (manual):**
```powershell
.\connect-to-server.ps1
```

**Automated check:**
```powershell
.\deploy-step1-check-environment.ps1
```

Then share the output with me, and we'll proceed to the next step!
