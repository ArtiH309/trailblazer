"""
Favorites API router

Endpoints:
- POST/trails/{trail_id}/favorite (toggle favorite for current user, toggle it again and it removes from favorite)
- GET/me/favorites (list current user's favorited trails)
"""

from typing import Generator, List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.db import SessionLocal
from app.models import Favorite, Trail, User
from app import schemas

from app.callback import get_current_user


router = APIRouter(prefix="", tags=["favorites"])


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.post(
    "/trails/{trail_id}/favorite",
    response_model=schemas.FavoriteStatusOut,
)
def toggle_favorite_trail(
    trail_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Toggle favorite status for a trail for the current user

    If not currently favorited then it wil favorite
    If already favorited then it removes favorite
    """
    trail = db.get(Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Trail not found")

    fav = (
        db.query(Favorite)
        .filter(
            Favorite.trail_id == trail_id,
            Favorite.user_id == current_user.id,
        )
        .first()
    )

    if fav:
        # Currently favorited then remove it
        db.delete(fav)
        db.commit()
        return schemas.FavoriteStatusOut(
            ok=True,
            is_favorited=False,
            message="Removed from favorites",
        )

    # Not favorited yet then add it
    new_fav = Favorite(
        user_id=current_user.id,
        trail_id=trail_id,
    )
    db.add(new_fav)
    db.commit()
    db.refresh(new_fav)

    return schemas.FavoriteStatusOut(
        ok=True,
        is_favorited=True,
        message="Added to favorites",
    )


@router.get(
    "/me/favorites",
    response_model=List[schemas.TrailOut],
)
def list_my_favorite_trails(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    List all trails that the user has favorited
    """
    trails = (
        db.query(Trail)
        .join(Favorite, Favorite.trail_id == Trail.id)
        .filter(Favorite.user_id == current_user.id)
        .order_by(Trail.name.asc())
        .all()
    )

    return trails
