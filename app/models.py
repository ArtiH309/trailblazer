"""
ORM models for Trailblazer

These classes map to database tables. Each attribute is a column.
We start with our main four tables: User, Park, Trail, Reviews.

Later we can add:
- TrailPoints (line for trail shapes)
- Photos (user uploaded images)
- Notes (private user notes)
- Lists/ListItems (favorites & collections)
- Follows (social)
- ActivityLogs (hike progress)
"""

from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy import String, Integer, Float, Text, Boolean, ForeignKey, CheckConstraint
from .db import Base


class User(Base): # actual creation of a user comes in auth
    __tablename__ = "users"

    # Set as Primary Key which will be an auto increment integer
    id: Mapped[int] = mapped_column(primary_key=True)

    # Email should be unique, allowing index makes the search for the email faster
    email: Mapped[str] = mapped_column(String, unique=True, index=True)

    # Store a password as a hash
    password_hash: Mapped[str] = mapped_column(String)

    # Display name -- CookinUpMemez would be tuff here
    display_name: Mapped[str] = mapped_column(String)



# Park -- can either be official or imported from NPS API
class Park(Base):
    __tablename__ = "parks"

    id: Mapped[int] = mapped_column(primary_key=True)

    # NPS provided ID if available or null if it is added by a user
    nps_id: Mapped[str | None] = mapped_column(String, nullable=True)

    # Park name and then can also be searched for fast
    name: Mapped[str] = mapped_column(String, index=True)

    # state location
    state: Mapped[str | None] = mapped_column(String, nullable=True)

    # Centerish coordinates for the park, can be null as well
    lat: Mapped[float | None] = mapped_column(Float, nullable=True)
    lon: Mapped[float | None] = mapped_column(Float, nullable=True)



class Trail(Base):
    __tablename__ = "trails"

    id: Mapped[int] = mapped_column(primary_key=True)

    # Link to a park if known from NPS, or null so the community trails outside NPS can be included
    park_id: Mapped[int | None] = mapped_column(ForeignKey("parks.id"), nullable=True)

    # Trail name
    name: Mapped[str] = mapped_column(String, index=True)

    # Difficulty kept is a string right now (easy, medium, hard), we can discuss if we want something else
    difficulty: Mapped[str] = mapped_column(String, default="moderate")

    # metrics for the trail, KM or maybe we can change to MI, but i think KM is a more official way
    length_km: Mapped[float | None] = mapped_column(Float, nullable=True)
    elevation_gain_m: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Check for accessibility or some cool features
    accessible: Mapped[bool] = mapped_column(Boolean, default=False)
    has_waterfall: Mapped[bool] = mapped_column(Boolean, default=False)
    has_viewpoint: Mapped[bool] = mapped_column(Boolean, default=False)

    # Coordinates, and then we can use this for nearby scanning
    lat: Mapped[float | None] = mapped_column(Float, nullable=True)
    lon: Mapped[float | None] = mapped_column(Float, nullable=True)

    # will add this to the parks as well later
    avg_rating: Mapped[float] = mapped_column(Float, default=0)
    ratings_count: Mapped[int] = mapped_column(Integer, default=0)

    # Create a relationship bc a trail has many reviews -- also add this to the parks as well
    reviews: Mapped[list["Review"]] = relationship(
        back_populates="trail",
        cascade="all, delete-orphan"  # removing a trail also removes its reviews
    )


class Review(Base):
    __tablename__ = "reviews"

    id: Mapped[int] = mapped_column(primary_key=True)

    # Foreign keys to the trail being reviewed and the user leaving the review
    trail_id: Mapped[int] = mapped_column(ForeignKey("trails.id"))
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"))

    # 1–5 integer rating
    rating: Mapped[int] = mapped_column(Integer)
    body: Mapped[str | None] = mapped_column(Text, nullable=True)

    # Make sure we only have 1-5 right now, will function as just buttons later
    __table_args__ = (CheckConstraint("rating BETWEEN 1 AND 5"),)

    # Relationship back to trail
    trail: Mapped["Trail"] = relationship(back_populates="reviews")
