# Secure API (FastAPI)

This repo implements AuthN (JWT RS256), refresh rotation (httpOnly cookie), RBAC, CORS, rate limits, and basic products/orders routes.

## 1) Prereqs
- Python 3.11+
- Docker + docker compose (for Postgres)

## 2) Clone & install
```bash
python -m venv .venv
# Windows: .venv\Scripts\activate
# macOS/Linux:
source .venv/bin/activate
pip install -r requirements.txt
```

## 3) Start Postgres

```bash
docker compose up -d
```

## 4) Create env

```bash
cp .env.example .env
# Generate dev RSA keys
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
# Open the files and paste into .env as JWT_PRIVATE_KEY and JWT_PUBLIC_KEY (escape newlines as \n)
```

## 5) Run the API

```bash
uvicorn app.main:app --reload --port 8000
```

## 6) Test the flow

* **Register**: `POST /auth/register` `{ "email": "user@example.com", "password": "VeryStrongPass!123" }`
* **Login**: `POST /auth/login` → returns `{ accessToken }` and sets `refresh` cookie
* **Public**: `GET /products`
* **Forbidden as USER**: `GET /orders` → 403
* **Promote to ADMIN**: In DB, update roles to `["USER", "ADMIN"]` for your user
* **Authorized**: `GET /orders` → 200 with `Authorization: Bearer <access>`
* **My orders**: `GET /orders/me` → from token subject
* **Refresh**: `POST /auth/refresh` → rotates refresh, returns new access token
* **Logout**: `POST /auth/logout` → revokes refresh, clears cookie

## 7) Notes

* Tables are auto-created at startup for dev; use Alembic migrations for production.
* Set `COOKIE_SECURE=true` and a real `COOKIE_DOMAIN` in prod.
* Configure `CORS_ORIGINS` to an explicit allowlist.
* Add `@limiter.limit("20/minute")` decorators per-route if you want granular limits.

## Security Features

✅ **JWT RS256** authentication with public/private key pairs  
✅ **Refresh token rotation** with httpOnly cookies  
✅ **RBAC** (Role-Based Access Control) system  
✅ **Anti-IDOR** protection with user scoping  
✅ **Rate limiting** with slowapi  
✅ **Input validation** with Pydantic  
✅ **Security headers** middleware  
✅ **CORS** configuration  
✅ **Password hashing** with bcrypt  
✅ **Refresh token hashing** in database
