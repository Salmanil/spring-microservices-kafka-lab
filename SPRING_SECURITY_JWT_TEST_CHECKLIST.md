# Spring Security JWT Test Checklist

Use this checklist to practice the auth flow end to end.

## Before You Start

Make sure these are up:

- `service-registry`
- `employee-api`
- database file with `users`, `roles`, `user_roles`, `token_blacklist`

Useful URLs:

- HTTP app: `http://localhost:8081`
- HTTPS app: `https://localhost:8443`
- Swagger: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

Important note:

- for `https://localhost:8443`, Postman may require SSL verification to be turned off because the local certificate is self-signed

## 1. Register Success

Test case:

- create a new user and get tokens

Step:

`POST http://localhost:8081/auth/register`

```json
{
  "username": "salmauser",
  "password": "Password123"
}
```

Expected result:

- HTTP `200`
- response contains:
  - `accessToken`
  - `refreshToken`
  - `tokenType`
  - `username`
  - `roles`

Why it matters:

- proves registration, password hashing, role mapping, and token generation

## 2. Register Validation Failure

Test case:

- invalid register request should fail

Step:

`POST http://localhost:8081/auth/register`

```json
{
  "username": "",
  "password": "short"
}
```

Expected result:

- HTTP `400`
- validation errors returned

Why it matters:

- proves input validation is working before business logic

## 3. Register Duplicate User

Test case:

- same username should not be created twice

Step:

send the same successful register request again

Expected result:

- HTTP `401`
- message like `Username already exists`

Why it matters:

- proves duplicate user protection

## 4. Login Success

Test case:

- existing user can log in

Step:

`POST http://localhost:8081/auth/login`

```json
{
  "username": "salmauser",
  "password": "Password123"
}
```

Expected result:

- HTTP `200`
- new access token
- new refresh token

Why it matters:

- proves username/password authentication

## 5. Login Failure

Test case:

- wrong password should fail

Step:

`POST http://localhost:8081/auth/login`

```json
{
  "username": "salmauser",
  "password": "WrongPassword"
}
```

Expected result:

- HTTP `401`

Why it matters:

- proves invalid credentials are blocked

## 6. Protected API Without Token

Test case:

- protected endpoint should reject anonymous request

Step:

`GET http://localhost:8081/employees/9702`

Expected result:

- HTTP `403` or `401` depending on how client receives the security response

Why it matters:

- proves security is active on business APIs

## 7. Protected API With Access Token

Test case:

- access token should allow business API call

Step:

`GET http://localhost:8081/employees/9702`

Header:

```text
Authorization: Bearer <access_token_here>
```

Expected result:

- HTTP `200`
- employee response body

Why it matters:

- proves JWT authentication filter is working

## 8. Protected API With Refresh Token

Test case:

- refresh token should not work as access token

Step:

use refresh token in:

```text
Authorization: Bearer <refresh_token_here>
```

Expected result:

- request is rejected

Why it matters:

- proves access and refresh tokens have different purposes

## 9. Refresh Success

Test case:

- refresh token should rotate tokens

Step:

`POST http://localhost:8081/auth/refresh`

```json
{
  "refreshToken": "<refresh_token_here>"
}
```

Expected result:

- HTTP `200`
- new access token
- new refresh token
- old refresh token inserted into `token_blacklist`

Why it matters:

- proves token rotation and blacklist persistence

## 10. Reuse Old Refresh Token

Test case:

- blacklisted old refresh token should fail

Step:

send the same old refresh token again to `/auth/refresh`

Expected result:

- HTTP `401`
- message like `Refresh token has been blacklisted`

Why it matters:

- proves replay protection is working

## 11. Logout Success

Test case:

- logout should blacklist refresh token

Step:

`POST http://localhost:8081/auth/logout`

```json
{
  "refreshToken": "<refresh_token_here>"
}
```

Expected result:

- HTTP `200`
- response message confirms blacklist

Why it matters:

- proves logout invalidates refresh token server-side

## 12. Refresh After Logout

Test case:

- logged-out refresh token must not work

Step:

send the logged-out refresh token to `/auth/refresh`

Expected result:

- HTTP `401`

Why it matters:

- proves logout is real invalidation, not just a client-side delete

## 13. Check DB Blacklist Rows

Test case:

- verify refresh token blacklist in database

Step:

```sql
select * from token_blacklist tb order by id desc;
```

Expected result:

- new rows with:
  - token
  - expiry_date

Why it matters:

- proves blacklist is persisted in your existing table

## 14. HTTPS Success

Test case:

- auth works over HTTPS

Step:

`POST https://localhost:8443/auth/login`

```json
{
  "username": "salmauser",
  "password": "Password123"
}
```

Expected result:

- HTTP `200`
- tokens returned

Why it matters:

- proves local HTTPS listener is working

## 15. HTTPS Local Certificate Issue

Test case:

- Postman rejects self-signed local certificate

Step:

call `https://localhost:8443/...` with SSL verification enabled

Expected result:

- SSL certificate error may appear

Resolution:

- disable SSL verification in Postman for local testing

Why it matters:

- this is common in local HTTPS labs

## 16. Missing Roles Table Data

Test case:

- registration fails if default role is missing

Step:

remove or rename `ROLE_USER` from `roles`

Expected result:

- register fails
- message says default role is missing

Why it matters:

- proves registration depends on database role data

## 17. Missing Authorization Header

Test case:

- protected call without bearer header

Step:

call a protected endpoint without `Authorization`

Expected result:

- rejected request

Why it matters:

- proves token is required

## 18. Invalid Bearer Token

Test case:

- fake token should fail

Step:

Header:

```text
Authorization: Bearer abc.def.ghi
```

Expected result:

- rejected request

Why it matters:

- proves JWT parsing/validation is active

## Best Practice Test Order

1. register success
2. login success
3. access protected API with token
4. access protected API without token
5. refresh success
6. reuse old refresh token
7. logout
8. refresh after logout
9. check `token_blacklist`
10. repeat one login on HTTPS

## Simple Troubleshooting

If `/auth/register` or `/auth/login` fails:

- check app is restarted after code changes
- check `ROLE_USER` exists in `roles`
- check DB path in `application.properties`

If protected endpoint still opens without token:

- check app restarted
- check `SecurityConfig`

If refresh does not blacklist:

- check `token_blacklist` table
- check `TokenBlacklistService`

If HTTPS fails:

- try `https://localhost:8443`
- disable SSL verification in Postman for local testing
