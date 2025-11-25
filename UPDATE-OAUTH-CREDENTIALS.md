# Quick Reference: Update OAuth Credentials

## After you create your Google OAuth credentials, update these 2 files:

---

### 📁 File 1: Frontend - SignInScreen.kt

**Location:** `Microtask-master/src/jsMain/kotlin/ui/screens/adminFeatures/signIn/SignInScreen.kt`

**Current line 37:**
```kotlin
attr("data-client_id", "876038700640-9md13do96fvdkc98vt8jelbbqnv8nsci.apps.googleusercontent.com")
```

**Replace with:**
```kotlin
attr("data-client_id", "YOUR_CLIENT_ID_HERE.apps.googleusercontent.com")
```

**Example:**
```kotlin
attr("data-client_id", "123456789-abcdefghijklmnop.apps.googleusercontent.com")
```

---

### 📁 File 2: Backend - application.properties

**Location:** `MicrotaskToolApi-master/src/main/resources/application.properties`

**Add at the END of the file:**
```properties
## Google OAuth 2.0 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID_HERE.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET_HERE
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
```

**Example:**
```properties
## Google OAuth 2.0 Configuration
spring.security.oauth2.client.registration.google.client-id=123456789-abcdefghijklmnop.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=GOCSPX-ABcd1234EFgh5678
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
```

---

## ⚠️ IMPORTANT NOTES:

1. **Client ID format:** Always ends with `.apps.googleusercontent.com`
2. **Client Secret format:** Always starts with `GOCSPX-`
3. **Never commit these to Git** - keep them private!
4. **Test users:** Add your email in Google Console as a test user

---

## 🧪 Testing Checklist:

After updating both files:

- [ ] Client ID updated in `SignInScreen.kt`
- [ ] Client ID and Secret added to `application.properties`
- [ ] Email added as Test User in Google Console
- [ ] Backend running: `.\gradlew.bat bootRun`
- [ ] Visit: `http://localhost:8080/#/admin/sign-in`
- [ ] Google Sign-In button appears
- [ ] Can click and see Google consent screen
- [ ] Can select test user account
- [ ] Successfully redirected after authorization

---

## 🔧 Google Console Settings Required:

**Authorized JavaScript origins:**
- `http://localhost:8080`
- `http://localhost:3000`

**Authorized redirect URIs:**
- `http://localhost:8080/login/oauth2/code/google`
- `http://localhost:8080`

**Test Users:**
- Your email address

---

## Need Help?

See the full detailed guide: `GOOGLE-OAUTH-SETUP.md`
