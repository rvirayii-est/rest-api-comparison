# Secure API (Spring Boot, Maven)

Implements JWT (RS256) access tokens, httpOnly refresh rotation, RBAC, CORS, validation.

## 1) Prereqs
- Java 21+
- Maven 3.9+
- Docker + docker compose (for Postgres)

## 2) Start Postgres
```bash
docker compose up -d
```

## 3) Create RSA keys

```bash
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
```

Copy the PEM contents into `src/main/resources/application.yml` under `app.jwt.private-key` and `app.jwt.public-key`.

## 4) Build & run

```bash
mvn spring-boot:run
```

## 5) Test the flow

* **Register**: `POST /auth/register` `{ "email": "user@example.com", "password": "VeryStrongPass!123" }`
* **Login**: `POST /auth/login` → returns `{ accessToken }` and sets `refresh` cookie
* **Public**: `GET /products`
* **Forbidden as USER**: `GET /orders` → 403 (needs ADMIN)
* **Promote to ADMIN**: Update your user roles in DB to `["USER","ADMIN"]`
* **Authorized**: `GET /orders` with `Authorization: Bearer <access>` → 200
* **My orders**: `GET /orders/me` (reads subject from JWT)
* **Refresh**: `POST /auth/refresh` → rotates refresh cookie & returns new access token
* **Logout**: `POST /auth/logout` → revokes refresh & clears cookie

## 6) Notes

* For production: store keys in a secret manager; configure a JWKS endpoint if needed.
* Enable CSRF only when using cookies for stateful sessions (not needed here; stateless JWT).
* Replace `ddl-auto: update` with migrations.
* Configure `app.cors-origins` and `cookie-secure: true` in prod.

## Security Features

✅ **JWT RS256** authentication with public/private key pairs  
✅ **Refresh token rotation** with httpOnly cookies  
✅ **RBAC** (Role-Based Access Control) system  
✅ **Anti-IDOR** protection with user scoping  
✅ **Input validation** with Bean Validation  
✅ **Security headers** with Spring Security  
✅ **CORS** configuration  
✅ **Password hashing** with BCrypt  
✅ **Refresh token hashing** in database (SHA256)  

## Architecture

The API includes:
- **Spring Boot 3.3.x** with Java 21
- **Spring Security** with OAuth2 Resource Server
- **Spring Data JPA** with PostgreSQL
- **Bean Validation** for input validation
- **Lombok** for boilerplate reduction
- **Nimbus JOSE + JWT** for RS256 token handling
