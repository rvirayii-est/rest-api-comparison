from fastapi import APIRouter, Depends, HTTPException, Response, Request, status
from sqlalchemy.orm import Session
from datetime import datetime
from ..db import get_db
from ..models import User, RefreshToken
from ..schemas import RegisterIn, LoginIn, TokenOut, MeOut
from ..security import get_password_hash, verify_password, sign_access_token, sign_refresh_token, sha256_hex, decode_token
from ..config import settings
from ..deps import get_current_user

router = APIRouter(prefix="/auth", tags=["auth"])

@router.post("/register")
def register(body: RegisterIn, db: Session = Depends(get_db)):
    exists = db.query(User).filter(User.email == body.email).first()
    if exists:
        raise HTTPException(status_code=400, detail="Email already registered")
    user = User(email=body.email, password_hash=get_password_hash(body.password), roles=["USER"])
    db.add(user)
    db.commit()
    return {"message": "registered"}

@router.post("/login", response_model=TokenOut)
def login(body: LoginIn, response: Response, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == body.email).first()
    if not user or not verify_password(body.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

    payload = {"sub": str(user.id), "email": user.email, "roles": user.roles}
    access = sign_access_token(payload)
    refresh = sign_refresh_token(str(user.id))

    # store hashed refresh token
    rec = RefreshToken(user_id=user.id, token_hash=sha256_hex(refresh))
    db.add(rec)
    db.commit()

    set_refresh_cookie(response, refresh)
    return {"accessToken": access}

@router.post("/refresh", response_model=TokenOut)
def refresh(request: Request, response: Response, db: Session = Depends(get_db)):
    token = request.cookies.get(settings.REFRESH_COOKIE_NAME)
    if not token:
        raise HTTPException(status_code=401, detail="No refresh token")

    # verify signature/exp
    try:
        payload = decode_token(token)
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid refresh token")

    token_hash = sha256_hex(token)
    rec = db.query(RefreshToken).filter(RefreshToken.token_hash == token_hash, RefreshToken.revoked_at.is_(None)).first()
    if not rec:
        raise HTTPException(status_code=401, detail="Refresh token revoked or unknown")

    # rotate: revoke old, issue new
    rec.revoked_at = datetime.utcnow()
    db.add(rec)

    user = db.query(User).filter(User.id == rec.user_id).first()
    new_refresh = sign_refresh_token(str(user.id))
    db.add(RefreshToken(user_id=user.id, token_hash=sha256_hex(new_refresh)))

    db.commit()

    access = sign_access_token({"sub": str(user.id), "email": user.email, "roles": user.roles})
    set_refresh_cookie(response, new_refresh)
    return {"accessToken": access}

@router.post("/logout")
def logout(request: Request, response: Response, db: Session = Depends(get_db)):
    token = request.cookies.get(settings.REFRESH_COOKIE_NAME)
    if token:
        token_hash = sha256_hex(token)
        rec = db.query(RefreshToken).filter(RefreshToken.token_hash == token_hash, RefreshToken.revoked_at.is_(None)).first()
        if rec:
            rec.revoked_at = datetime.utcnow()
            db.add(rec)
            db.commit()
    clear_refresh_cookie(response)
    return {"message": "logged out"}

@router.get("/me", response_model=MeOut)
def me(user=Depends(get_current_user)):
    return {"sub": user["sub"], "email": user.get("email"), "roles": user.get("roles", [])}

# Cookie helpers

def set_refresh_cookie(response: Response, token: str):
    response.set_cookie(
        key=settings.REFRESH_COOKIE_NAME,
        value=token,
        httponly=True,
        secure=settings.COOKIE_SECURE,
        samesite=settings.COOKIE_SAMESITE,  # 'strict' recommended
        domain=settings.COOKIE_DOMAIN,
        path="/auth",
        max_age=settings.REFRESH_TOKEN_TTL_SECONDS,
    )

def clear_refresh_cookie(response: Response):
    response.delete_cookie(
        key=settings.REFRESH_COOKIE_NAME,
        domain=settings.COOKIE_DOMAIN,
        path="/auth",
    )
