"""
Activities + Progress API router

Endpoints:
- POST/trails/{trail_id}/activities (log an activity for a trail)
- GET/activities/me (list current user's activities)
- GET/progress/me (averages the stats for the user)
"""

from typing import Generator, List, Optional
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from sqlalchemy import func

from app.db import SessionLocal
from app.models import Activity, Trail, User
from app import schemas


from app.callback import get_current_user


router = APIRouter(prefix="", tags=["activities"])


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.post(
    "/trails/{trail_id}/activities",
    response_model=schemas.ActivityOut,
    status_code=status.HTTP_201_CREATED,
)
def log_activity_for_trail(
    trail_id: int,
    data: schemas.ActivityCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Log a hike/activity on a trail for the user
    """
    trail = db.get(Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=404, detail="Trail not found")

    activity = Activity(
        user_id=current_user.id,
        trail_id=trail_id,
        distance_km=data.distance_km,
        duration_min=data.duration_min,
        elevation_gain_m=data.elevation_gain_m,
    )

    # Can override the date if provided, else DB default will set it to the time when the progress is uploaded
    if data.date is not None:
        activity.date = data.date

    db.add(activity)
    db.commit()
    db.refresh(activity)

    return activity


@router.get(
    "/activities/me",
    response_model=List[schemas.ActivityOut],
)
def list_my_activities(
    trail_id: Optional[int] = Query(
        default=None,
        description="Optional trail_id filter",
    ),
    date_from: Optional[datetime] = Query(
        default=None,
        description="Optional start date/time filter",
    ),
    date_to: Optional[datetime] = Query(
        default=None,
        description="Optional end date/time filter",
    ),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    List the current user's activities, with optional filters
    """
    q = db.query(Activity).filter(Activity.user_id == current_user.id)

    if trail_id is not None:
        q = q.filter(Activity.trail_id == trail_id)

    if date_from is not None:
        q = q.filter(Activity.date >= date_from)

    if date_to is not None:
        q = q.filter(Activity.date <= date_to)

    activities = (
        q.order_by(Activity.date.desc(), Activity.created_at.desc())
        .all()
    )

    return activities


@router.get(
    "/progress/me",
    response_model=schemas.ProgressOut,
)
def get_my_progress(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Averages the stats for the current user's activities:
    - total distance
    - total activities
    - unique trails completed
    - averages
    - last activity timestamp
    """
    q = (
        db.query(
            func.coalesce(func.sum(Activity.distance_km), 0.0),
            func.count(Activity.id),
            func.count(func.distinct(Activity.trail_id)),
            func.avg(Activity.distance_km),
            func.avg(Activity.duration_min),
            func.max(Activity.date),
        )
        .filter(Activity.user_id == current_user.id)
    )

    (
        total_distance_km,
        total_activities,
        trails_completed,
        avg_distance_km,
        avg_duration_min,
        last_activity_at,
    ) = q.one()

    # avg can be None if no activities or no values so leave as None for that case
    return schemas.ProgressOut(
        total_distance_km=float(total_distance_km or 0.0),
        total_activities=int(total_activities or 0),
        trails_completed=int(trails_completed or 0),
        avg_distance_km=float(avg_distance_km) if avg_distance_km is not None else None,
        avg_duration_min=float(avg_duration_min) if avg_duration_min is not None else None,
        last_activity_at=last_activity_at,
    )
