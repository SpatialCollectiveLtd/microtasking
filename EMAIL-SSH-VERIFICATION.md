# Email Draft: SSH Access Verification Request

---

**Subject:** SSH Access Issue - Need Credential Verification for DPW Server

---

**Dear [Name],**

I'm attempting to set up SSH access to the DPW server for deployment purposes, but I'm encountering authentication issues. I need your help to verify the connection details.

## Current Situation

I'm trying to connect via SSH but getting "Permission denied" errors, even though:
- ✅ The server is reachable and responding
- ✅ SSH port 22 is open and accepting connections
- ✅ The server accepts password authentication
- ❌ The password appears to be incorrect or the credentials don't match

## Information You Provided

Based on previous communication, I have:

**Server IP Address:**
- ~~102.220.23.109~~ (incorrect)
- **102.210.149.40** (correct)

**Admin Account:**
- Username: `admin`
- Password: `DPw!@2025`

**Root Account:**
- Username: `root`
- Password: `DPW!@2025!`

**Note:** You mentioned "You can only remote login using admin account" and "Root login is after admin through su root"

## What I Need You to Verify

Please confirm or correct the following:

### 1. Server IP Address
- [x] Correct IP address confirmed: **102.210.149.40**
- [ ] Are there any firewall rules or VPN requirements I should know about?

### 2. Admin Credentials
- [ ] Username: Is it exactly `admin` (lowercase)?
- [ ] Password: Please verify the EXACT password (copy/paste it to ensure accuracy)
  - Current attempt: `DPw!@2025`
  - Character breakdown: D-P-w-!-@-2-0-2-5
- [ ] Are there any spaces, special characters, or hidden formatting?

### 3. Authentication Method
- [ ] Should I be using password authentication?
- [ ] Or do I need to set up SSH keys?
- [ ] Is the admin account enabled for remote SSH login?

### 4. Alternative Access
If password authentication continues to fail:
- [ ] Can you provide access to a server management panel?
- [ ] Can you reset the admin password?
- [ ] Is there a web-based console/terminal available?
- [ ] Should I try connecting as root directly instead?

## Technical Details

For your reference, here's what the SSH connection shows:
- Server OS: Ubuntu (OpenSSH 8.9p1 Ubuntu-3ubuntu0.13)
- SSH Port: 22 (confirmed open)
- Authentication methods available: publickey, password
- Error message: "Permission denied" after password entry

## Next Steps

Once you verify the credentials, I'll need to:
1. Successfully SSH into the server
2. Install required software (Java, MySQL, Nginx)
3. Deploy the microtasking application
4. Configure SSL and domain settings

**Could you please respond with:**
1. ✅ Confirmed correct IP address
2. ✅ Confirmed correct username
3. ✅ The exact password (copy/paste to avoid typos)
4. ✅ Any additional connection requirements

Thank you for your assistance!

---

**Best regards,**
[Your Name]

---

## Attachment Suggestion

If helpful, you can attach:
- Screenshot of the SSH error
- Server connection test results
- This technical summary

---

## Quick Copy/Paste Section

**For their easy response, include this:**

```
Please fill in the correct values:

SSH Connection Details:
- IP Address: ___________________
- Port: ___________________
- Username: ___________________
- Password: ___________________
- Any VPN/Firewall notes: ___________________

Alternative Access:
- Management Panel URL: ___________________
- Panel Username: ___________________
- Panel Password: ___________________
```
