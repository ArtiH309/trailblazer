"""
Profiles API router

Endpoints:
- GET/profiles/me (view my profile)
- PATCH /profiles/me (update my profile)
- GET /profiles/{user_id} (public view of another user's profile)
"""

from typing import Generator

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.db import SessionLocal
from app.models import Profile, User
from app import schemas


from app.callback import get_current_user


router = APIRouter(prefix="/profiles", tags=["profiles"])


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def _get_or_create_profile(db: Session, user: User) -> Profile:
    profile = db.get(Profile, user.id)
    if profile:
        return profile

    profile = Profile(
        user_id=user.id,
        avatar_url=None,
        bio=None,
        home_state=None,
        home_lat=None,
        home_lon=None,
    )
    db.add(profile)
    db.commit()
    db.refresh(profile)
    return profile


def _to_profile_out(user: User, profile: Profile) -> schemas.ProfileOut:
    return schemas.ProfileOut(
        user_id=user.id,
        display_name=user.display_name,
        avatar_url=profile.avatar_url,
        bio=profile.bio,
        home_state=profile.home_state,
        home_lat=profile.home_lat,
        home_lon=profile.home_lon,
    )


@router.get("/me", response_model=schemas.ProfileOut)
def get_my_profile(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Get the current user's profile
    If it doesn't exist yet, create a default empty profile
    """
    profile = _get_or_create_profile(db, current_user)
    return _to_profile_out(current_user, profile)


@router.patch("/me", response_model=schemas.ProfileOut)
def update_my_profile(
    data: schemas.ProfileUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Update the current user's profile
    Creates a profile if it doesn't exist yet
    """
    profile = _get_or_create_profile(db, current_user)

    update_data = data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(profile, field, value)

    db.commit()
    db.refresh(profile)

    return _to_profile_out(current_user, profile)


@router.get("/{user_id}", response_model=schemas.ProfileOut)
def get_profile_by_user_id(
    user_id: int,
    db: Session = Depends(get_db),
):
    """
    Public view of a user's profile by user_id
    For now everything is public
    """
    user = db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    profile = db.get(Profile, user_id)
    if not profile:
        profile = Profile(
            user_id=user.id,
            avatar_url=None,
            bio=None,
            home_state=None,
            home_lat=None,
            home_lon=None,
        )
        db.add(profile)
        db.commit()
        db.refresh(profile)

    return _to_profile_out(user, profile)
