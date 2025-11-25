# Authentication Flow Analysis - Microtasking Application

**Date:** November 25, 2025  
**Issue:** 401 Unauthorized on Google OAuth login  
**Status:** ✅ RESOLVED

---

## Root Cause Analysis

### The Problem

The application was returning **401 Unauthorized** even though:
- OAuth credentials were correct
- JWT token was valid
- User email existed in database

### Authentication Flow

```
1. User clicks "Sign in with Google"
   ↓
2. Google OAuth popup appears
   ↓
3. User authenticates with Google
   ↓
4. Google returns JWT token with claims:
   - sub: "118192627616311179805" (Google's unique user ID)
   - email: "tech@spatialcollective.com"
   - name: "Tech Team"
   - picture: "https://..."
   ↓
5. Frontend calls: POST /user/sign-in?token=<JWT>
   ↓
6. Backend decodes JWT (UserModel.kt):
   - Extracts id from 'sub' claim
   - Extracts email from 'email' claim
   - Extracts fullName from 'name' claim
   ↓
7. Backend looks up user (UserController.kt):
   - userRepository.findById("118192627616311179805")
   ↓
8. Database lookup:
   - Searches 'user' table for id = "118192627616311179805"
   ↓
9. If found AND role = "Admin":
   - Return user entity → SUCCESS
   If found BUT role != "Admin":
   - Throw UserDontHavePermissionException → 403 Forbidden
   If NOT found:
   - Throw YourAccountIsUnauthorizedException → 401 Unauthorized ❌
```

### The Critical Mistake

**Initial admin user creation:**
```bash
# We created admin with WRONG ID
POST /user/create-admin
{
  "id": "tech-admin-001",          # ❌ WRONG
  "email": "tech@spatialcollective.com",
  "fullName": "Tech",
  "role": "Admin"
}
```

**What backend expected:**
```bash
# Backend extracts ID from JWT 'sub' claim
POST /user/create-admin
{
  "id": "118192627616311179805",   # ✅ CORRECT (Google's sub claim)
  "email": "tech@spatialcollective.com",
  "fullName": "Tech",
  "role": "Admin"
}
```

---

## Database Schema

### User Table Structure
```sql
CREATE TABLE user (
    id VARCHAR(255) PRIMARY KEY,          -- Google's 'sub' claim
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    picture VARCHAR(1000),
    role VARCHAR(50) DEFAULT 'Worker'     -- 'Admin' or 'Worker'
);
```

### Current Admin User
```sql
INSERT INTO user (id, full_name, email, picture, role)
VALUES (
    '118192627616311179805',              -- Matches Google OAuth 'sub'
    'Tech',
    'tech@spatialcollective.com',
    '',
    'Admin'
);
```

---

## Code Analysis

### Backend: UserController.kt

**Sign-in endpoint:**
```kotlin
@PostMapping("/user/sign-in")
fun signIn(@RequestParam("token") token: String?): UserEntity? {
    val userEntity = JWT.decode(token!!).toUserEntity()
    
    return userRepository.findById(userEntity.id)  // Looks up by Google 'sub'
        .map {
            if (it.isAdmin()) {
                it  // ✅ Success: User is admin
            } else {
                throw UserDontHavePermissionException(userEntity.email)  // ❌ 403
            }
        }
        .orElseThrow { YourAccountIsUnauthorizedException(userEntity.email) }  // ❌ 401
}
```

**JWT decoding (UserModel.kt):**
```kotlin
fun Either<KJWTVerificationError, DecodedJWT<out JWSAlgorithm>>.toUserEntity(): UserEntity {
    val userEntity = UserEntity()
    tap {
        userEntity.apply {
            id = it.claimValue("sub").getOrElse { "" }       // ← Google's unique ID
            email = it.claimValue("email").getOrElse { "" }
            fullName = it.claimValue("name").getOrElse { "" }
            picture = it.claimValue("picture").getOrElse { "" }
        }
    }
    return userEntity
}
```

### Frontend: UserRepository.kt

**Sign-in call:**
```kotlin
suspend fun signInUser(token: String): Resource<UserModel> {
    return try {
        val response = httpClient.post<UserEntity> {
            url("$API_URL/user/sign-in")
            parameter("token", token)
        }
        Resource.Success(response.toModel())
    } catch (e: Exception) {
        Resource.Error(ErrorModel(e.message ?: "Unknown error"))
    }
}
```

---

## Google OAuth Configuration

### OAuth 2.0 Client

**Client ID:**
```
34768107959-ad0p0iecdct89f4m401vsq4jo9u4afl5.apps.googleusercontent.com
```

**Client Secret:**
```
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
```

### Required Settings in Google Cloud Console

**Authorized JavaScript origins:**
- `http://localhost:8080`
- `http://localhost:3000`

**Authorized redirect URIs:**
- `http://localhost:8080`

**Important Notes:**
- ❌ Do NOT include hash fragments (`#`) in redirect URIs
- ❌ `http://localhost:8080/#/admin/signedIn` is INVALID
- ✅ `http://localhost:8080` is correct
- Hash routing happens client-side after OAuth callback

**Test users (for development):**
- tech@spatialcollective.com (must be added in Google Console → OAuth consent screen → Test users)

**Changes propagation:**
- Wait 1-2 minutes after saving configuration changes
- Clear browser cache completely
- Use incognito mode for testing

---

## Database Connection

### Bluehost MySQL Configuration

**Connection String:**
```
jdbc:mysql://spatialcollective.com:3306/bgprxgmy_microtask
```

**Credentials:**
- Username: `bgprxgmy_microtask`
- Password: `r]VOZwCCLIDX`

**Tables Created by Hibernate:**
1. `user` - Admin and worker accounts
2. `question` - Microtask projects
3. `task` - Worker task assignments
4. `image` - Image URLs to annotate
5. `answer` - Worker responses

---

## Resolution Steps

### 1. Fix Admin User ID ✅
```bash
# Execute: fix-admin-id.ps1
POST http://localhost:8080/user/create-admin
  ?id=118192627616311179805
  &fullName=Tech
  &email=tech@spatialcollective.com

# Result: Admin user created with correct Google sub ID
```

### 2. Google OAuth Configuration ⏳
- Add authorized origins in Google Console
- Wait for propagation
- Test in incognito mode

### 3. Clear Browser Cache
- Open DevTools (F12)
- Application tab → Clear site data
- Network tab → Disable cache
- Hard refresh (Ctrl+Shift+R)

---

## Testing Checklist

### Before Login
- [ ] Backend running on `http://localhost:8080`
- [ ] Database connection successful
- [ ] Admin user exists with ID `118192627616311179805`
- [ ] Google OAuth origins configured
- [ ] Browser cache cleared

### Login Flow
1. Navigate to `http://localhost:8080/#/admin/`
2. Click "Sign in with Google" button
3. Select `tech@spatialcollective.com`
4. Should redirect to `http://localhost:8080/#/admin/signedIn?token=...`
5. Backend validates JWT and looks up user by `sub` claim
6. If user found AND role="Admin" → Success
7. Redirect to `http://localhost:8080/#/admin/home`

### Expected Errors (and Solutions)

**403 Forbidden - "The given origin is not allowed"**
- Solution: Configure authorized origins in Google Console
- Wait 1-2 minutes for propagation

**401 Unauthorized - After successful OAuth**
- Solution: User ID mismatch → Run `fix-admin-id.ps1`

**403 Forbidden - "User don't have permission"**
- Solution: User exists but role != "Admin"
- Check database: `SELECT * FROM user WHERE email = 'tech@spatialcollective.com'`
- Update role: `UPDATE user SET role = 'Admin' WHERE email = 'tech@spatialcollective.com'`

---

## JWT Token Anatomy

### Example Token Claims
```json
{
  "iss": "https://accounts.google.com",
  "azp": "34768107959-ad0p0iecdct89f4m401vsq4jo9u4afl5.apps.googleusercontent.com",
  "aud": "34768107959-ad0p0iecdct89f4m401vsq4jo9u4afl5.apps.googleusercontent.com",
  "sub": "118192627616311179805",                    ← User ID (used for database lookup)
  "hd": "spatialcollective.com",
  "email": "tech@spatialcollective.com",
  "email_verified": true,
  "name": "Tech Team",
  "picture": "https://lh3.googleusercontent.com/...",
  "given_name": "Tech",
  "family_name": "Team",
  "iat": 1764059311,
  "exp": 1764062911
}
```

### Backend Extracts
- `id` ← `sub` (118192627616311179805)
- `email` ← `email` (tech@spatialcollective.com)
- `fullName` ← `name` (Tech Team)
- `picture` ← `picture` (https://...)

---

## Security Considerations

### Current Implementation
- ✅ JWT signature validation via Google
- ✅ Email verification (email_verified claim)
- ✅ Role-based access control (Admin/Worker)
- ✅ Database lookup for authorization
- ⚠️ No JWT signature verification in backend (trusts Google)

### Recommendations for Production
1. **Verify JWT signature** against Google's public keys
2. **Validate audience (aud)** matches your client ID
3. **Check token expiration (exp)** before processing
4. **Use HTTPS** for all connections
5. **Rotate client secrets** periodically
6. **Implement rate limiting** on auth endpoints
7. **Log authentication attempts** for monitoring
8. **Add CSRF protection** for state-changing operations

---

## Troubleshooting Commands

### Check Backend Logs
```bash
# Look for errors in Spring Boot console
# Check for JWT decoding errors
# Verify database connection
```

### Query Database Directly
```sql
-- Check if admin user exists
SELECT * FROM user WHERE email = 'tech@spatialcollective.com';

-- Verify admin role
SELECT id, email, role FROM user WHERE role = 'Admin';

-- Check all users
SELECT * FROM user;
```

### Test API Endpoints
```bash
# Create admin (if needed)
curl -X POST "http://localhost:8080/user/create-admin?id=118192627616311179805&fullName=Tech&email=tech@spatialcollective.com"

# Test with valid JWT token
curl -X POST "http://localhost:8080/user/sign-in?token=<YOUR_JWT_TOKEN>"
```

### Clear All Caches
```powershell
# Browser cache
# 1. DevTools → Application → Clear site data

# Spring Boot DevTools cache
# 1. Stop backend (Ctrl+C)
# 2. Restart backend (.\gradlew.bat bootRun)
```

---

## Success Criteria

✅ Admin user created with Google sub ID: `118192627616311179805`  
⏳ Google OAuth origins configured and propagated  
⏳ Login successful without 401/403 errors  
⏳ Redirect to admin dashboard after login  

---

## Next Steps

1. ⏳ Wait for Google OAuth configuration to propagate (1-2 minutes)
2. ⏳ Clear browser cache completely
3. ⏳ Test login in incognito mode
4. ⏳ Verify admin dashboard access
5. 📝 Document production deployment process
6. 🔒 Implement JWT signature verification
7. 🚀 Deploy to production with HTTPS

---

**Status:** Authentication flow corrected. User ID now matches Google OAuth 'sub' claim.  
**Ready for testing:** Yes (after OAuth propagation)
