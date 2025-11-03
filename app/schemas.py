"""
Difference between this and models.py is that this is what is outputted to the user.
"""

from typing import Optional
from pydantic import BaseModel, Field


class MsgOut(BaseModel):
   # Simple message for successful responses
    ok: bool = True
    message: str = "success"


class TrailOut(BaseModel):
 # Only show the user what needs to be shown
    id: int
    name: str
    difficulty: str
    length_km: Optional[float] = None
    elevation_gain_m: Optional[float] = None
    lat: Optional[float] = None
    lon: Optional[float] = None
    accessible: bool = False
    has_waterfall: bool = False
    has_viewpoint: bool = False
    avg_rating: float = 0.0
    ratings_count: int = 0

    # Tells Pydantic to read data directly from SQLAlchemy models
    class Config:
        from_attributes = True


class TrailCreate(BaseModel):
  # users can create their trail
    name: str = Field(min_length=1, max_length=200)
    difficulty: str = Field(default="moderate")
    length_km: Optional[float] = Field(default=None, ge=0)
    elevation_gain_m: Optional[float] = Field(default=None, ge=0)
    lat: Optional[float] = Field(default=None, ge=-90, le=90)
    lon: Optional[float] = Field(default=None, ge=-180, le=180)
    accessible: bool = False
    has_waterfall: bool = False
    has_viewpoint: bool = False
    park_id: Optional[int] = None


class ReviewCreate(BaseModel):
# information needed by user to add a review
    rating: int = Field(ge=1, le=5) # ge and le means greater or equal / less or equal
    body: Optional[str] = Field(default=None, max_length=2000)


class ReviewOut(BaseModel):
    id: int
    trail_id: int
    user_id: int
    rating: int
    body: Optional[str] = None

    class Config:
        from_attributes = True


class ParkOut(BaseModel):
    id: int
    name: str
    state: Optional[str] = None
    lat: Optional[float] = None
    lon: Optional[float] = None

    class Config:
        from_attributes = True
