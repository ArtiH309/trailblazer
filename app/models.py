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
from sqlalchemy import String, Integer, Float, Text, Boolean, ForeignKey, CheckConstraint, DateTime, func, UniqueConstraint
from .db import Base
from datetime import datetime #for photos when photos get uploaded


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

    notes: Mapped[list["Note"]] = relationship(back_populates="user", cascade="all, delete-orphan")

    favorites: Mapped[list["Favorite"]] = relationship(back_populates="user", cascade="all, delete-orphan")

    activities: Mapped[list["Activity"]] = relationship(back_populates="user", cascade="all, delete-orphan")

    profile: Mapped[list["Profile"]] = relationship(back_populates="user", cascade="all, delete-orphan", uselist=False)
    # the uselist establishes this to be a one to one relationship for the user and its profile

    posts: Mapped[list["Post"]] = relationship(back_populates="user", cascade="all, delete-orphan")

    offline_downloads: Mapped[list["OfflineDownload"]] = relationship(back_populates="user", cascade="all, delete-orphan")



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

    notes: Mapped[list["Note"]] = relationship(back_populates="trail", cascade="all, delete-orphan")

    favorites: Mapped[list["Favorite"]] = relationship(back_populates="trail", cascade="all, delete-orphan")

    activities: Mapped[list["Activity"]] = relationship(back_populates="trail", cascade="all, delete-orphan")

    posts: Mapped[list["Post"]] = relationship(back_populates="trail", cascade="all, delete-orphan")

    offline_downloads: Mapped[list["OfflineDownload"]] = relationship(back_populates="trail", cascade="all, delete-orphan")


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


class Photos(Base):
    __tablename__ = "photos"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True) # photo id #
    trail_id: Mapped[int] = mapped_column(ForeignKey("trails.id"), index=True, nullable=False) # photos need a trail, nullable=False bc we need it 
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), index=True, nullable=False) # photos need a user 
    file_path: Mapped[str] = mapped_column(String, nullable=False) # stored in a path
    caption: Mapped[str] = mapped_column(Text, nullable=True) # caption for the photo
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False) # the time when the photo was uploaded


class Note(Base):
    __tablename__ = "notes"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    trail_id: Mapped[int] = mapped_column(ForeignKey("trails.id", ondelete="CASCADE"), index=True) # ondelete=CASCADE deletes the row if the note is deleted
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True)
    text: Mapped[str] = mapped_column(Text, nullable=False)
    is_pinned: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)
    trail: Mapped["Trail"] = relationship(back_populates="notes")
    user: Mapped["User"] = relationship(back_populates="notes")



class Favorite(Base):
    __tablename__ = "favorites"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True)
    trail_id: Mapped[int] = mapped_column(ForeignKey("trails.id", ondelete="CASCADE"), index=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    user: Mapped["User"] = relationship(back_populates="favorites")
    trail: Mapped["Trail"] = relationship(back_populates="favorites")

    __table_args__ = (
        UniqueConstraint("user_id", "trail_id", name="uq_favorites_user_trail"))



class Activity(Base):
    __tablename__ = "activities"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)

    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True)
    trail_id: Mapped[int] = mapped_column(ForeignKey("trails.id", ondelete="CASCADE"), index=True)

    # When the hike/activity happened
    date: Mapped[datetime] = mapped_column(DateTime(timezone=True),server_default=func.now(), nullable=False)

    distance_km: Mapped[float | None] = mapped_column(Float, nullable=True)
    duration_min: Mapped[int | None] = mapped_column(Integer, nullable=True)
    elevation_gain_m: Mapped[float | None] = mapped_column(Float, nullable=True)

    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(),nullable=False)

    user: Mapped["User"] = relationship(back_populates="activities")
    trail: Mapped["Trail"] = relationship(back_populates="activities")


class Profile(Base):
    __tablename__ = "profiles"

    # user_id is the primary key which is one to one with User
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)

    avatar_url: Mapped[str | None] = mapped_column(String, nullable=True)
    bio: Mapped[str | None] = mapped_column(Text, nullable=True)
    home_state: Mapped[str | None] = mapped_column(String, nullable=True)
    home_lat: Mapped[float | None] = mapped_column(Float, nullable=True)
    home_lon: Mapped[float | None] = mapped_column(Float, nullable=True)

    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(),nullable=False)

    user: Mapped["User"] = relationship(back_populates="profile")



class Post(Base):
    __tablename__ = "posts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)

    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"),index=True)
    trail_id: Mapped[int | None] = mapped_column(ForeignKey("trails.id", ondelete="SET NULL"), nullable=True, index=True)

    title: Mapped[str | None] = mapped_column(String, nullable=True)
    body: Mapped[str] = mapped_column(Text, nullable=False)

    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)

    user: Mapped["User"] = relationship(back_populates="posts")
    trail: Mapped["Trail"] = relationship(back_populates="posts")



class OfflineDownload(Base):
    __tablename__ = "offline_downloads"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)

    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True)
    trail_id: Mapped[int] = mapped_column(ForeignKey("trails.id", ondelete="CASCADE"), index=True)

    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    user: Mapped["User"] = relationship(back_populates="offline_downloads")
    trail: Mapped["Trail"] = relationship(back_populates="offline_downloads")

    __table_args__ = (
        UniqueConstraint("user_id", "trail_id", name="uq_offline_user_trail"))
