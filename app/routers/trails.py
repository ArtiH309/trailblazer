"""
Trails API router.

Endpoints:
- GET /trails/ = list trails filter by nearby lat/lon + radius
- GET /trails/search = search trails by name
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

from fastapi import APIRouter, Depends, HTTPException, Query, UploadFile, File, Form
from datetime import datetime
import os
import pathlib
from sqlalchemy.orm import Session
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError

from app.db import SessionLocal
from app import models, schemas

from app.callback import get_current_user, get_db
from app.models import User, Trail, Review, Photos

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
    R = 6371.0  # this is earths radius
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
        ),  # description and examples dont actually run anything -- it shows up in the docs for the localhost
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

    if not near:  # this means that if the query string provided has no lat/lon to look through
        return q.limit(100).all()

    # Parse near="lat,lon"
    try:
        lat_s, lon_s = map(float, near.split(","))  # break up into the two different points of lat/lon
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid 'near' format. Use 'lat,lon'.")

    q = q.filter(models.Trail.lat.isnot(None), models.Trail.lon.isnot(None))

    results: list[models.Trail] = []
    for t in q.all():
        d_km = haversine_km(lat_s, lon_s, float(t.lat), float(t.lon))  # run the haversine formula to find nearby
        if d_km <= radius:  # only give results within a certain radius
            results.append(t)

    # Sort by rating decreasing, then length increasing
    return sorted(results, key=lambda t: (-t.avg_rating, t.length_km or 1e9))[:50]


# ADD THIS NEW ENDPOINT
@router.get("/search", response_model=List[schemas.TrailOut])
def search_trails(
        q: str = Query(description="Search query for trail name"),
        near: Optional[str] = Query(default=None, description="Optional 'lat,lon' for distance sorting"),
        limit: int = Query(default=50, ge=1, le=100),
        db: Session = Depends(get_db),
):
    """
    Search trails by name (case-insensitive partial match)
    Optionally sort by distance if 'near' lat,lon is provided
    """
    # Case-insensitive search on trail name
    query = db.query(models.Trail).filter(
        models.Trail.name.ilike(f"%{q}%")
    )

    trails = query.limit(limit).all()

    # If near is provided, sort by distance
    if near:
        try:
            lat_s, lon_s = map(float, near.split(","))
            results_with_dist = []
            for trail in trails:
                if trail.lat and trail.lon:
                    dist = haversine_km(lat_s, lon_s, float(trail.lat), float(trail.lon))
                    results_with_dist.append((dist, trail))

            # Sort by distance and return trails
            results_with_dist.sort(key=lambda x: x[0])
            return [trail for _, trail in results_with_dist]
        except Exception:
            pass  # If parsing fails, just return unsorted results

    return trails


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
        text("SELECT ROUND(AVG(rating), 2) FROM reviews WHERE trail_id = :tid"),
        # we look at the trail, then the reviews of the trail, and then take the avg of the ratings and round it
        {"tid": trail_id},  # define the trail id as tid
    ).scalar() or 0.0
    count = db.execute(
        text("SELECT COUNT(*) FROM reviews WHERE trail_id = :tid"),  # count how many reviews a trail has
        {"tid": trail_id},
    ).scalar() or 0
    trail.avg_rating, trail.ratings_count = float(avg), int(count)
    db.commit()

    return schemas.MsgOut(ok=True, message="Review added")


# GET /trails/{trail_id}/reviews (this is to see the reviews)
@router.get("/{trail_id}/reviews", response_model=List[schemas.ReviewOut])
def list_reviews_for_trail(
        trail_id: int,
        db: Session = Depends(get_db),
):
    # make sure the trail actually exists
    trail = db.get(Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=404, detail="Trail not found")

    # sort by newest trails first
    reviews = (
        db.query(Review)
        .filter(Review.trail_id == trail_id)
        .all()
    )
    return reviews


def safe_file(
        original: str) -> str:  # this just cleans the filename so there shouldnt be any path components in the name.
    name = os.path.basename(original)
    return name


@router.post("/{trail_id}/photos", response_model=schemas.PhotosOut)
def upload_photo(
        # uploads a photo to the trail, requires auth of user,
        trail_id: int,
        file: UploadFile = File(..., description="Uploaded image file"),
        caption: str | None = Form(default=None),
        db: Session = Depends(get_db),
        current_user: User = Depends(get_current_user)
):
    trail = db.get(Trail, trail_id)

    if not trail:
        raise HTTPException(status_code=404, detail="Trail not found")

    if not (file.content_type or "").startswith("image/"):
        raise HTTPException(status_code=400, detail="Only images allowed")

    folder = pathlib.Path("media") / "trails" / str(trail_id)
    folder.mkdir(parents=True, exist_ok=True)

    ts = int(datetime.utcnow().timestamp())
    safe_name = safe_file(file.filename or "upload.bin")
    dest_rel = pathlib.Path("trails") / str(trail_id) / f"{ts}_{safe_name}"
    dest_abs = pathlib.Path("media") / dest_rel

    # Save file
    with dest_abs.open("wb") as out:
        out.write(file.file.read())

    # Save DB row
    photo = Photos(
        trail_id=trail_id,
        user_id=current_user.id,
        file_path=str(dest_rel).replace("\\", "/"),
        caption=caption,
    )
    db.add(photo)
    db.commit()
    db.refresh(photo)

    # url that gets pushed out for users
    url = f"/media/{photo.file_path}"
    return schemas.PhotosOut(
        id=photo.id,
        trail_id=photo.trail_id,
        user_id=photo.user_id,
        caption=photo.caption,
        created_at=photo.created_at,
        url=url,
    )


@router.get("/{trail_id}/photos", response_model=list[schemas.PhotosOut])
def list_trail_photos(
        trail_id: int,
        db: Session = Depends(get_db),
):
    """
    List photos for a trail
    """
    trail = db.get(Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=404, detail="Trail not found")

    photos = (
        db.query(Photos)
        .filter(Photos.trail_id == trail_id)
        .order_by(Photos.created_at.desc())
        .all()
    )

    out: list[schemas.PhotosOut] = []
    for p in photos:
        out.append(
            schemas.PhotosOut(
                id=p.id,
                trail_id=p.trail_id,
                user_id=p.user_id,
                caption=p.caption,
                created_at=p.created_at,
                url=f"/media/{p.file_path}",
            )
        )
    return out
