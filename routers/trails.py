"""
Trails API router.

Endpoints:
- GET /trails/ = list trails filter by nearby lat/lon + radius
- GET /trails/{trail_id} = get one trail by id
- POST /trails/{trail_id}/reviews = add a review to a trail then it recomputes avg and count

GET is when the user retrieves data, and POST is when the user is uploading data.

Notes:
- Uses a Haversine distance function for nearby filtering.
- Uses a request  DB session dependency.
- Recomputes ratings after inserting a review.
"""

from typing import Generator, List, Optional
from math import radians, sin, cos, asin, sqrt

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError

from app.db import SessionLocal
from app import models, schemas

from app.callback import get_current_user
from app.models import User


router = APIRouter(prefix="/trails", tags=["trails"])


def get_db() -> Generator[Session, None, None]:
    # Create a new SQLAlchemy Session for this request and then close it after
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# Haversine formula in KM for nearby filtering -- gets nearby between TWO points
def haversine_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
   # Compute great circle distance between two (lat,lon) points in kilometers.
    R = 6371.0 # this is earths radius
    dlat = radians(lat2 - lat1)
    dlon = radians(lon2 - lon1)
    a = sin(dlat / 2) ** 2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlon / 2) ** 2
    return 2 * R * asin(sqrt(a))


# GET /trails/  (list + optional nearby filter)
@router.get("/", response_model=List[schemas.TrailOut]) 
# response model is how our output is given from the schemas, i.e. after a link that is / -- the information of trails is outputted with the data we defined in schema for Trail Out
def list_trails(
    near: Optional[str] = Query(
        default=None,
        description="Comma-separated 'lat,lon' to filter by nearby (e.g., '40.758,-73.9855').",
        examples=["40.758,-73.9855"],
    ), # description and examples dont actually run anything -- it shows up in the docs for the localhost
    radius: float = Query(
        default=50,
        ge=0.1,
        le=200,
        description="Search radius in kilometers when 'near' is provided (0.1–200).",
    ),
    db: Session = Depends(get_db),
):
    """
    Return up to 100 trails, or up to 50 nearby trails if we have near values.
    Sort nearby by avg_rating, then length_km. Good trails will have priority.
    """
    q = db.query(models.Trail)

    if not near: # this means that if the query string provided has no lat/lon to look through
        return q.limit(100).all()

    # Parse near="lat,lon"
    try:
        lat_s, lon_s = map(float, near.split(",")) # break up into the two different points of lat/lon
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid 'near' format. Use 'lat,lon'.")

    q = q.filter(models.Trail.lat.isnot(None), models.Trail.lon.isnot(None))

    results: list[models.Trail] = []
    for t in q.all():
        d_km = haversine_km(lat_s, lon_s, float(t.lat), float(t.lon)) # run the haversine formula to find nearby
        if d_km <= radius: # only give results within a certain radius
            results.append(t)

    # Sort by rating decreasing, then length increasing
    return sorted(results, key=lambda t: (-t.avg_rating, t.length_km or 1e9))[:50]



# GET /trails/{trail_id}
@router.get("/{trail_id}", response_model=schemas.TrailOut)
def get_trail(trail_id: int, db: Session = Depends(get_db)):
    trail = db.get(models.Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=404, detail="Trail not found")
    return trail


# POST /trails/{trail_id}/reviews  (add a review)
@router.post("/{trail_id}/reviews", response_model=schemas.MsgOut)
def add_review(
    trail_id: int,
    payload: schemas.ReviewCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),  # we need user authentication for a review to be added
):
    # 1) Trail must exist
    trail = db.get(models.Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=404, detail="Trail not found")

    # 2) Insert review (user id from JWT)
    review = models.Review(
        trail_id=trail_id,
        user_id=current_user.id,
        rating=payload.rating,
        body=payload.body,
    )
    db.add(review)
    try:
        db.commit()
    except IntegrityError as e:
        db.rollback()
        raise HTTPException(status_code=400, detail="Review could not be saved.") from e

    # 3) Recompute stats
    avg = db.execute(
        text("SELECT ROUND(AVG(rating), 2) FROM reviews WHERE trail_id = :tid"), # we look at the trail, then the reviews of the trail, and then take the avg of the ratings and round it
        {"tid": trail_id}, # define the trail id as tid
    ).scalar() or 0.0
    count = db.execute(
        text("SELECT COUNT(*) FROM reviews WHERE trail_id = :tid"), # count how many reviews a trail has
        {"tid": trail_id},
    ).scalar() or 0
    trail.avg_rating, trail.ratings_count = float(avg), int(count)
    db.commit()

    return schemas.MsgOut(ok=True, message="Review added")
