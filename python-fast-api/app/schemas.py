from pydantic import BaseModel, EmailStr, Field
from typing import List

class RegisterIn(BaseModel):
    email: EmailStr
    password: str = Field(min_length=12, max_length=72)

class LoginIn(BaseModel):
    email: EmailStr
    password: str

class MeOut(BaseModel):
    sub: str
    email: EmailStr
    roles: List[str]

class TokenOut(BaseModel):
    accessToken: str
