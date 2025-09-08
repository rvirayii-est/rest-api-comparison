# Secure API (NestJS)

This is a **complete, runnable** NestJS project implementing secure-by-default patterns:

* **AuthN**: Local email/password, **JWT (RS256)** access tokens (15m), **httpOnly refresh** cookies (7d) with rotation.
* **AuthZ**: RBAC via roles claim + route guard; anti‑IDOR user scoping.
* **Data protection**: Helmet, strict CORS, rate limiting, input validation, secrets via env, hashed refresh tokens, structured errors.
* **Persistence**: Postgres via TypeORM.

## Quick start

```bash
# 1) Clone and install
npm i

# 2) Bring up Postgres
docker compose up -d

# 3) Copy env and add values
cp .env.example .env
# Paste your RSA keys; or use openssl to generate (see below)

# 4) Run dev
npm run start:dev

# 5) Test flow
# Register → Login → /products → /orders (403) → make ADMIN → /orders (200) → refresh
```

### Generate dev RSA keys

```bash
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem

# put into .env as JWT_PRIVATE_KEY and JWT_PUBLIC_KEY (escape newlines as \n)
```

### Postman quick calls
- POST /auth/register { email, password }
- POST /auth/login → returns { accessToken } + sets refresh cookie
- GET  /products (public)
- GET  /orders (needs ADMIN)
- GET  /orders/me (needs USER)
- POST /auth/refresh (uses httpOnly cookie)
- POST /auth/logout (revokes refresh)

## Architecture

The API includes:
- **JWT RS256** authentication with refresh token rotation
- **RBAC** with role-based access control
- **Rate limiting** and security headers
- **TypeORM** with PostgreSQL
- **Input validation** with class-validator
- **httpOnly cookies** for refresh tokens
- **Anti-IDOR** protections with user scoping
