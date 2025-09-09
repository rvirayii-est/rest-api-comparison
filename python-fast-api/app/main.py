from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from starlette.responses import JSONResponse
from slowapi import Limiter
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from .config import settings
from .db import engine, Base
from .routers import auth, products, orders

app = FastAPI(title=settings.APP_NAME)

# Create tables (dev) – use Alembic in prod
Base.metadata.create_all(bind=engine)

# CORS
origins = settings.cors_list() or ["*"] if settings.ENV == "development" else []
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["Authorization", "Content-Type"],
)

# Rate limiting
limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter

@app.exception_handler(RateLimitExceeded)
async def rate_limit_handler(request: Request, exc: RateLimitExceeded):
    return JSONResponse(status_code=429, content={"detail": "Too Many Requests"})

# Security headers middleware
@app.middleware("http")
async def security_headers(request: Request, call_next):
    resp = await call_next(request)
    resp.headers.setdefault("X-Content-Type-Options", "nosniff")
    resp.headers.setdefault("X-Frame-Options", "DENY")
    resp.headers.setdefault("Referrer-Policy", "no-referrer")
    # Consider configuring Permissions-Policy/CSP based on your frontend
    return resp

# Routers
app.include_router(auth.router)
app.include_router(products.router)
app.include_router(orders.router)

@app.get("/")
async def health():
    return {"ok": True}
