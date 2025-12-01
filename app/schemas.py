"""
Difference between this and models.py is that this is what is outputted to the user.
"""

from typing import Optional
from pydantic import BaseModel, Field
from datetime import datetime


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


class PhotosOut(BaseModel):
    id: int
    trail_id: int
    user_id: int
    caption: Optional[str] = None
    url: str
    created_at: datetime

    class Config:
        from_attributes = True


class NoteBase(BaseModel):
    text: str = Field(min_length=1, max_length=2000)
    is_pinned: bool = False

class NoteCreate(NoteBase): # Same metrics as NoteBase, will be for POSTing a note
    pass

class NoteUpdate(BaseModel): # will be for PATCHing a note
    text: Optional[str] = Field(default=None, min_length=1, max_length=2000)
    is_pinned: Optional[bool] = None

class NoteOut(BaseModel): # returns the note
    id: int
    trail_id: int
    user_id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attribute = True


class FavoriteStatusOut(BaseModel): # basically just true for farvorited, false for not
    ok: bool = True
    is_favorited: bool
    message: str = "success"


class NpsImportOut(BaseModel): # lets us now insert parks
    ok: bool = True
    state: str
    inserted: int
    updated: int
    total: int



class ActivityCreate(BaseModel): # logs the user's activity for a trail. Date is optional. 
    date: Optional[datetime] = None
    distance_km: Optional[float] = Field(default=None, ge=0)
    duration_min: Optional[int] = Field(default=None, ge=0)
    elevation_gain_m: Optional[float] = Field(default=None, ge=0)


class ActivityOut(BaseModel):
    id: int
    user_id: int
    trail_id: int
    date: datetime
    distance_km: Optional[float] = None
    duration_min: Optional[int] = None
    elevation_gain_m: Optional[float] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True



class ProgressOut(BaseModel): # shows the user's average progess and all that 
    total_distance_km: float = 0.0
    total_activities: int = 0
    trails_completed: int = 0
    avg_distance_km: Optional[float] = None
    avg_duration_min: Optional[float] = None
    last_activity_at: Optional[datetime] = None



class ProfileBase(BaseModel):
    avatar_url: Optional[str] = None
    bio: Optional[str] = Field(default=None, max_length=500)
    home_state: Optional[str] = Field(
        default=None,
        description="2-letter state code like NY, NH.",
        min_length=2,
        max_length=2,
    )
    home_lat: Optional[float] = Field(default=None, ge=-90, le=90)
    home_lon: Optional[float] = Field(default=None, ge=-180, le=180)


class ProfileUpdate(ProfileBase):
    pass


class ProfileOut(ProfileBase):
    user_id: int
    display_name: str # from User

    class Config:
        from_attributes = True



class PostBase(BaseModel):
    trail_id: Optional[int] = None
    title: Optional[str] = Field(default=None, max_length=200)
    body: str = Field(min_length=1, max_length=4000)


class PostCreate(PostBase):
    # same as base for now
    pass


class PostUpdate(BaseModel):
    title: Optional[str] = Field(default=None, max_length=200)
    body: Optional[str] = Field(default=None, min_length=1, max_length=4000)


class PostOut(PostBase):
    id: int
    user_id: int
    created_at: datetime
    updated_at: datetime
    display_name: str  # from User, we fill this manually

    class Config:
        from_attributes = True


class OfflineStatusOut(BaseModel):
    ok: bool = True
    is_offline: bool
    message: str = "success"
