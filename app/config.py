"""
This is basically holding the settings for the app.

Explaining how JWT works:
JSON Web Token
It is a long string created by an algorithm (HS256) that contains three pieces of information:
The header -- which is the "type" of JWT it is (algorithm)
The payload -- the information. So consists of userID, their role, token expiration.
The signature -- with the header, the payload, and the secret... the signature is formed. A payload that has user, gets a different signature than a payload with admin, so we can tell who has user vs admin permission.
"""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    DATABASE_URL: str = "sqlite:///./trailblazer.db"
    JWT_SECRET: str = "secret-token-in-env" # overrides in .env 
    JWT_ALG: str = "HS256" # algorithm to sign tokens with
    JWT_EXPIRE_MINUTES: int = 60 * 24 * 7 # 7 days
    CORS_ALLOW_ORIGINS: str = "*"

    NPS_API_KEY: str | None = None

    class Config:
        env_file = ".env"


settings = Settings()
