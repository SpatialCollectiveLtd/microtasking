# Google OAuth 2.0 Setup Guide for Microtask Application

## Step-by-Step Process to Create Google OAuth Credentials

### Step 1: Access Google Cloud Console
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Sign in with your Google account (the one you want to use for this project)

### Step 2: Create a New Project
1. Click on the **project dropdown** at the top (next to "Google Cloud")
2. Click **"NEW PROJECT"**
3. Enter project details:
   - **Project Name**: `Microtask Tool` (or any name you prefer)
   - **Organization**: Leave as "No organization" (unless you have one)
4. Click **"CREATE"**
5. Wait for the project to be created (10-30 seconds)
6. Select your new project from the dropdown

### Step 3: Enable Google+ API (Required for OAuth)
1. In the left sidebar, go to **"APIs & Services"** → **"Library"**
2. Search for **"Google+ API"** (or "Google Identity")
3. Click on **"Google+ API"**
4. Click **"ENABLE"**
5. Wait for it to enable

### Step 4: Configure OAuth Consent Screen
1. Go to **"APIs & Services"** → **"OAuth consent screen"**
2. Choose **"External"** (unless you have a Google Workspace)
3. Click **"CREATE"**
4. Fill in the required information:

   **App Information:**
   - App name: `Microtask Tool`
   - User support email: `your-email@gmail.com` (select from dropdown)
   - App logo: (Optional - you can skip for testing)

   **App Domain (Optional for testing):**
   - Application home page: `http://localhost:8080`
   - Leave others blank for now

   **Developer Contact Information:**
   - Email addresses: `your-email@gmail.com`

5. Click **"SAVE AND CONTINUE"**

6. **Scopes Screen:**
   - Click **"ADD OR REMOVE SCOPES"**
   - Select these scopes:
     - `email` (See your email address)
     - `profile` (See your personal info)
     - `openid` (Authenticate using OpenID Connect)
   - Click **"UPDATE"**
   - Click **"SAVE AND CONTINUE"**

7. **Test Users Screen:**
   - Click **"ADD USERS"**
   - Add your email address (and any other emails you want to test with)
   - Click **"ADD"**
   - Click **"SAVE AND CONTINUE"**

8. **Summary Screen:**
   - Review your settings
   - Click **"BACK TO DASHBOARD"**

### Step 5: Create OAuth 2.0 Credentials
1. Go to **"APIs & Services"** → **"Credentials"**
2. Click **"+ CREATE CREDENTIALS"** at the top
3. Select **"OAuth client ID"**
4. Choose Application type:
   - Select **"Web application"**
5. Fill in the details:

   **Name:** `Microtask Web Client`

   **Authorized JavaScript origins:**
   - Click **"+ ADD URI"**
   - Add: `http://localhost:8080`
   - Click **"+ ADD URI"** again
   - Add: `http://localhost:3000` (for frontend if running separately)

   **Authorized redirect URIs:**
   - Click **"+ ADD URI"**
   - Add: `http://localhost:8080/login/oauth2/code/google`
   - Click **"+ ADD URI"** again  
   - Add: `http://localhost:8080`

6. Click **"CREATE"**

### Step 6: Get Your Credentials
1. A popup will appear with your credentials:
   - **Client ID**: `123456789-xxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com`
   - **Client Secret**: `GOCSPX-xxxxxxxxxxxxxxxxxxxxx`
2. **IMPORTANT**: Copy both values immediately
3. Click **"DOWNLOAD JSON"** (save this file securely - you might need it later)
4. Click **"OK"**

---

## Step 7: Update Your Application Files

Once you have your **Client ID** and **Client Secret**, follow these steps:

### A. Update Frontend (SignInScreen.kt)

**File:** `Microtask-master/src/jsMain/kotlin/ui/screens/adminFeatures/signIn/SignInScreen.kt`

**Find this line (around line 37):**
```kotlin
attr("data-client_id", "876038700640-9md13do96fvdkc98vt8jelbbqnv8nsci.apps.googleusercontent.com")
```

**Replace with YOUR Client ID:**
```kotlin
attr("data-client_id", "YOUR-CLIENT-ID-HERE.apps.googleusercontent.com")
```

### B. Update Backend (application.properties)

**File:** `MicrotaskToolApi-master/src/main/resources/application.properties`

**Add these lines at the end:**
```properties
# Google OAuth 2.0 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR-CLIENT-ID-HERE.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=YOUR-CLIENT-SECRET-HERE
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
```

---

## Step 8: Update Frontend HTML (Load Google OAuth Script)

**File:** `Microtask-master/src/jsMain/resources/index.html`

Make sure you have the Google Sign-In script loaded in the `<head>` section:
```html
<script src="https://accounts.google.com/gsi/client" async defer></script>
```

---

## Testing Your OAuth Setup

### 1. Start Backend Server
```bash
cd MicrotaskToolApi-master
.\gradlew.bat bootRun
```
Wait for: "Tomcat started on port(s): 8080"

### 2. Start Frontend (if separate)
```bash
cd Microtask-master
.\gradlew.bat jsBrowserRun
```

### 3. Test Login
1. Open browser: `http://localhost:8080` (or frontend URL)
2. Navigate to admin sign-in page: `http://localhost:8080/#/admin/sign-in`
3. Click the **"Sign in with Google"** button
4. You should see Google's consent screen
5. Select your test account
6. Authorize the application
7. You should be redirected back and logged in

---

## Troubleshooting Common Issues

### Error: "Access blocked: This app's request is invalid"
- **Fix**: Make sure you added your email as a "Test User" in OAuth consent screen

### Error: "redirect_uri_mismatch"
- **Fix**: Check that the redirect URI in Google Console exactly matches what your app is using
- Must be: `http://localhost:8080/login/oauth2/code/google`

### Error: "invalid_client"
- **Fix**: Double-check your Client ID and Client Secret are correct

### Google Sign-In button not showing
- **Fix**: 
  1. Check browser console for JavaScript errors
  2. Verify Google script is loaded: `<script src="https://accounts.google.com/gsi/client" async defer></script>`
  3. Clear browser cache

### "Origin not allowed"
- **Fix**: Add `http://localhost:8080` to "Authorized JavaScript origins" in Google Console

---

## Important Notes

1. **Test Users**: While your app is in "Testing" mode, only test users you explicitly add can sign in
2. **Production**: To publish your app, you'll need to verify your domain and complete the OAuth verification process
3. **Security**: Never commit your Client Secret to version control (add to `.gitignore`)
4. **Localhost Only**: Current setup only works on localhost. For production, you'll need to:
   - Add production domain to authorized origins
   - Update redirect URIs to production URLs
   - Update application.properties with production URLs

---

## Production Deployment (Future)

When deploying to production (e.g., `https://microtasktool.spatialcollective.com`):

1. **Update Google Console:**
   - Add: `https://microtasktool.spatialcollective.com` to Authorized JavaScript origins
   - Add: `https://microtasktool.spatialcollective.com/login/oauth2/code/google` to redirect URIs

2. **Update Frontend:**
   - Change Client ID to production Client ID (if different)

3. **Update Backend:**
   - Update `redirect-uri` to production URL in application.properties

4. **Verify Domain:**
   - Google may require domain verification for production use
   - Follow the verification process in Google Console

---

## Your Credentials Template

**Save this securely (DO NOT COMMIT TO GIT):**

```
Google OAuth 2.0 Credentials - Microtask Tool

Client ID: ____________________________________________.apps.googleusercontent.com

Client Secret: GOCSPX-___________________________________

Test Users:
- your-email@gmail.com

Created: [Date]
Project: Microtask Tool
Environment: Development (localhost)
```

---

## Next Steps After Setup

1. ✅ Create Google OAuth credentials
2. ✅ Update SignInScreen.kt with your Client ID
3. ✅ Update application.properties with Client ID and Secret
4. ✅ Test login on localhost
5. 📝 Create first admin user
6. 📝 Test question creation
7. 📝 Test task assignment
8. 📝 Upload images to server
9. 📝 Deploy to production
