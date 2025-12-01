"""
Offline downloads metadata API router

Endpoints:
- POST/offline/trails/{trail_id} (toggle offline save for current user)
- GET/offline/trails (list trails saved for offline use)
"""

from typing import Generator, List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.db import SessionLocal
from app.models import OfflineDownload, Trail, User
from app import schemas

from app.callback import get_current_user


router = APIRouter(prefix="/offline", tags=["offline"])


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.post(
    "/trails/{trail_id}",
    response_model=schemas.OfflineStatusOut,
)
def toggle_offline_trail(
    trail_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    - If not currently saved -> create an OfflineDownload
    - If already saved -> remove it
    """
    trail = db.get(Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Trail not found")

    record = (
        db.query(OfflineDownload)
        .filter(
            OfflineDownload.trail_id == trail_id,
            OfflineDownload.user_id == current_user.id,
        )
        .first()
    )

    if record:
        # Already offline -> remove
        db.delete(record)
        db.commit()
        return schemas.OfflineStatusOut(
            ok=True,
            is_offline=False,
            message="Removed from offline list",
        )

    # Not offline yet -> add
    new_record = OfflineDownload(
        user_id=current_user.id,
        trail_id=trail_id,
    )
    db.add(new_record)
    db.commit()
    db.refresh(new_record)

    return schemas.OfflineStatusOut(
        ok=True,
        is_offline=True,
        message="Saved for offline use",
    )


@router.get(
    "/trails",
    response_model=List[schemas.TrailOut],
)
def list_offline_trails(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    List all trails the user has marked for offline use
    """
    trails = (
        db.query(Trail)
        .join(OfflineDownload, OfflineDownload.trail_id == Trail.id)
        .filter(OfflineDownload.user_id == current_user.id)
        .order_by(Trail.name.asc())
        .all()
    )

    return trails
