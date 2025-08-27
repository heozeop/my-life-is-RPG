# API Key Authentication Configuration

## Overview

MyLiftIsRPG uses environment-based API key authentication for secure access to protected endpoints. This system supports role-based access control and can be configured via environment variables for different deployment environments.

## Configuration Format

API keys are configured using environment variables with the following format:

```
KEY:USER_ID:USERNAME:ROLE1,ROLE2
```

Multiple users can be separated by the pipe (`|`) character:

```
KEY1:ID1:NAME1:ROLES|KEY2:ID2:NAME2:ROLES
```

## Environment Variables

### `API_KEYS_ADMIN`
Defines admin users with elevated permissions.

**Format:** `key:userId:username:roles`

**Example:**
```bash
export API_KEYS_ADMIN="ak_prod_admin_12345678901234567890:1:admin:USER,ADMIN"
```

### `API_KEYS_USERS` 
Defines regular users and other non-admin roles.

**Format:** `key:userId:username:roles`

**Example:**
```bash
export API_KEYS_USERS="ak_prod_user_12345678901234567890:2:user:USER|ak_beta_12345678901234567890:3:beta:USER,BETA"
```

## Available Roles

- **USER**: Basic authenticated access to API endpoints
- **ADMIN**: Full administrative access including config endpoints
- **BETA**: Beta tester access (custom role)
- **SUPER**: Super admin access (custom role)

## API Key Format Standards

### Production Keys
```
ak_prod_[purpose]_[32_char_random_string]
```

### Development Keys  
```
ak_dev_[purpose]_[32_char_random_string]
```

### Environment Keys
```
ak_[env]_[purpose]_[32_char_random_string]
```

## Security Best Practices

1. **Key Length**: Minimum 32 characters after the prefix
2. **Entropy**: Use cryptographically secure random generation
3. **Rotation**: Implement regular key rotation policies
4. **Storage**: Never commit keys to version control
5. **Environment**: Use different keys for each environment

## Configuration Examples

### Development Environment

Create a `.env` file (ignored by git):

```bash
# Development API Keys
API_KEYS_ADMIN=ak_dev_admin_12345678901234567890123456:1:devadmin:USER,ADMIN
API_KEYS_USERS=ak_dev_user_123456789012345678901234567:2:devuser:USER
```

### Production Environment

Set environment variables in your deployment system:

```bash
# Production API Keys
export API_KEYS_ADMIN="ak_prod_admin_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6:10:prodadmin:USER,ADMIN"
export API_KEYS_USERS="ak_prod_user_x1y2z3a4b5c6d7e8f9g0h1i2j3k4l5m6:11:produser:USER|ak_prod_beta_p1q2r3s4t5u6v7w8x9y0z1a2b3c4d5e6:12:betatester:USER,BETA"
```

### Docker Environment

Using Docker Compose:

```yaml
services:
  mylifisrpg:
    environment:
      - API_KEYS_ADMIN=ak_docker_admin_12345678901234567890:1:dockeradmin:USER,ADMIN
      - API_KEYS_USERS=ak_docker_user_12345678901234567890:2:dockeruser:USER
```

## Testing Authentication

### Using curl

```bash
# Test with admin user
curl -H "X-API-KEY: ak_prod_admin_12345678901234567890" \
     http://localhost:8080/api/auth/me

# Test admin-only endpoint
curl -H "X-API-KEY: ak_prod_admin_12345678901234567890" \
     http://localhost:8080/api/auth/admin/config

# Test with regular user (should fail on admin endpoint)
curl -H "X-API-KEY: ak_prod_user_12345678901234567890" \
     http://localhost:8080/api/auth/admin/config
```

### Expected Response Formats

**Successful Authentication:**
```json
{
  "userId": 1,
  "username": "admin",
  "roles": ["USER", "ADMIN"],
  "isAdmin": true,
  "authenticatedAt": "2025-08-27T15:30:00",
  "authenticated": true,
  "timestamp": "2025-08-27T15:30:00"
}
```

**Authentication Failure:**
```json
{
  "status": 401,
  "error": "Unauthorized", 
  "message": "Invalid API key",
  "timestamp": "2025-08-27T15:30:00"
}
```

**Insufficient Permissions:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions", 
  "timestamp": "2025-08-27T15:30:00"
}
```

## Admin Configuration Endpoint

Admins can check the loaded configuration at:

```
GET /api/auth/admin/config
```

**Response:**
```json
{
  "loadedApiKeys": 3,
  "users": ["admin(USER,ADMIN)", "user(USER)", "beta(USER,BETA)"],
  "timestamp": "2025-08-27T15:30:00",
  "note": "API keys themselves are not exposed for security"
}
```

## Troubleshooting

### Common Issues

1. **Invalid Format**: Ensure colon separation and no extra spaces
2. **Missing Roles**: At least one role must be specified
3. **Duplicate Keys**: Each API key must be unique
4. **Invalid User IDs**: User IDs must be valid numbers

### Debug Logging

Enable debug logging to see configuration parsing:

```yaml
logging:
  level:
    com.mylifeisrpg.myliftisrpg.security: DEBUG
```

Look for these log messages:
- `Initialized API key authentication with X valid keys`
- `Loaded users: [username(roles), ...]`
- `Authentication failed for API key: xxxxxxxx...`

## Key Generation

### Using OpenSSL
```bash
# Generate random API key
echo "ak_prod_$(openssl rand -hex 16)"
```

### Using Python
```python
import secrets
import string

def generate_api_key(prefix="ak_prod"):
    random_part = ''.join(secrets.choice(string.ascii_lowercase + string.digits) 
                         for _ in range(32))
    return f"{prefix}_{random_part}"

print(generate_api_key())
```

## Migration from Hardcoded Keys

To migrate from hardcoded keys to environment-based configuration:

1. Generate new production keys
2. Set environment variables in deployment
3. Update client applications with new keys
4. Verify authentication works
5. Remove old hardcoded keys from code

## Support

For issues with API key authentication:
- Check application logs for authentication failures
- Verify environment variable format
- Test with curl or similar tools
- Use admin config endpoint to verify loaded configuration