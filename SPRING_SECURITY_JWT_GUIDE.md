# Spring Security JWT Guide

This guide explains the security flow added to `employee-api`.

## What Was Added

- Spring Security for protected APIs
- JWT access token
- JWT refresh token
- refresh token blacklist in the existing `token_blacklist` table
- input validation for auth requests
- HTTPS for local learning
- Swagger/OpenAPI remains open for learning

## Which Service Uses This

Security is added in:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api`

Main files:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\config\SecurityConfig.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\security\JwtAuthenticationFilter.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\security\JwtService.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\service\AuthService.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\controller\AuthController.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\service\TokenBlacklistService.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\config\HttpConnectorConfig.java`

## Database Tables Used

Existing tables used by the auth flow:

- `users`
- `roles`
- `user_roles`
- `token_blacklist`

Important learning point:

- user login data is stored in `users`
- user roles are linked through `user_roles`
- old refresh tokens are stored in `token_blacklist`

## Token Types

### Access Token

Purpose:

- used in `Authorization: Bearer <token>`
- allows access to protected APIs

Lifetime:

- `15` minutes

Behavior:

- checked by `JwtAuthenticationFilter`
- only access tokens are allowed for normal API calls

### Refresh Token

Purpose:

- used only to get a new access token and a new refresh token

Lifetime:

- `7` days

Behavior:

- sent to `/auth/refresh`
- old refresh token is blacklisted after refresh

## Blacklist Flow

Blacklist is only used for refresh tokens in this implementation.

When a refresh token gets blacklisted:

- on `/auth/refresh`
- on `/auth/logout`

What happens:

1. request comes with refresh token
2. app checks token validity
3. app stores that refresh token in `token_blacklist`
4. old refresh token cannot be reused

This is how token rotation is simulated.

## Protected and Open Endpoints

Open endpoints:

- `/auth/**`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/actuator/health`

Protected endpoints:

- everything else, including `/employees/**`

## HTTPS Setup

Both HTTP and HTTPS are available for local learning.

Ports:

- HTTP: `http://localhost:8081`
- HTTPS: `https://localhost:8443`

Why both are kept:

- existing local platform and gateway flow can continue to use `8081`
- you can still learn secure local calls with `8443`

Important note for Postman:

- the certificate is self-signed
- if Postman blocks the request, disable SSL certificate verification for local testing

## Security Flow

### Register

1. call `/auth/register`
2. user is created in `users`
3. default role `ROLE_USER` is linked
4. access token and refresh token are returned

### Login

1. call `/auth/login`
2. username and password are checked
3. access token and refresh token are returned

### Access Protected API

1. copy `accessToken`
2. send header:

```text
Authorization: Bearer <accessToken>
```

3. call `/employees/...`
4. filter validates token
5. request is allowed

### Refresh

1. call `/auth/refresh` with current refresh token
2. app validates it
3. app blacklists old refresh token
4. app returns new access token and new refresh token

### Logout

1. call `/auth/logout`
2. app blacklists that refresh token
3. same refresh token cannot be used again

## Input Validation

Validation is active for auth requests.

Examples:

- blank username fails
- blank password fails
- short password fails for register

Also, employee request validation already exists for the employee CRUD payloads.

## Example Requests

### Register

`POST http://localhost:8081/auth/register`

```json
{
  "username": "salmauser",
  "password": "Password123"
}
```

### Login

`POST http://localhost:8081/auth/login`

```json
{
  "username": "salmauser",
  "password": "Password123"
}
```

### Refresh

`POST http://localhost:8081/auth/refresh`

```json
{
  "refreshToken": "<refresh_token_here>"
}
```

### Logout

`POST http://localhost:8081/auth/logout`

```json
{
  "refreshToken": "<refresh_token_here>"
}
```

### Protected API Call

`GET http://localhost:8081/employees/9702`

Header:

```text
Authorization: Bearer <access_token_here>
```

## What To Check In DB

After refresh or logout, check:

```sql
select * from token_blacklist tb;
```

You should see:

- old refresh token
- expiry date of that token

## Simple Mental Model

- username/password gets you tokens
- access token opens protected APIs
- refresh token creates new tokens
- used refresh tokens are blacklisted
- blacklist prevents token reuse
- HTTPS gives a secure local testing path
