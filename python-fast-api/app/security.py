from datetime import datetime, timedelta, timezone
from typing import Any, Dict
from jose import jwt
from passlib.context import CryptContext
import hashlib
from .config import settings

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

ALGO = "RS256"

# Password hashing

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)

# Token helpers

def sign_access_token(payload: Dict[str, Any]) -> str:
    private_key = settings.JWT_PRIVATE_KEY.replace("\\n", "\n")
    exp = datetime.now(timezone.utc) + timedelta(seconds=settings.ACCESS_TOKEN_TTL_SECONDS)
    to_encode = {**payload, "exp": exp}
    return jwt.encode(to_encode, private_key, algorithm=ALGO)

def sign_refresh_token(subject: str) -> str:
    private_key = settings.JWT_PRIVATE_KEY.replace("\\n", "\n")
    exp = datetime.now(timezone.utc) + timedelta(seconds=settings.REFRESH_TOKEN_TTL_SECONDS)
    to_encode = {"sub": subject, "exp": exp}
    return jwt.encode(to_encode, private_key, algorithm=ALGO)


def decode_token(token: str) -> Dict[str, Any]:
    public_key = settings.JWT_PUBLIC_KEY.replace("\\n", "\n")
    return jwt.decode(token, public_key, algorithms=[ALGO])


def sha256_hex(token: str) -> str:
    return hashlib.sha256(token.encode()).hexdigest()
