"""
- get_db() for a request SQLAlchemy Session
- get_current_user() to enforce JWT auth and fetch the User
"""
from typing import Generator
from datetime import datetime, timezone
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import jwt

from app.db import SessionLocal
from app.models import User
from app.config import settings

# Reusable HTTP bearer (reads Authorization: Bearer <token>)
bearer_scheme = HTTPBearer(auto_error=True)


def get_db() -> Generator:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_current_user(
    creds: HTTPAuthorizationCredentials = Depends(bearer_scheme),
    db = Depends(get_db),
) -> User:
    # verifies the JWT, loads user from the db, and then runs checks to see if the user is valid
    token = creds.credentials
    try:
        payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALG])
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Invalid or expired token")

    user_id = payload.get("sub")
    exp_ts = payload.get("exp")

    if not user_id or not exp_ts:
        raise HTTPException(status_code=401, detail="Invalid token payload")

    # check to see if login expired
    if datetime.now(tz=timezone.utc).timestamp() > exp_ts:
        raise HTTPException(status_code=401, detail="Token expired")

    user = db.get(User, int(user_id))
    if not user:
        raise HTTPException(status_code=401, detail="User not found")

    return user
