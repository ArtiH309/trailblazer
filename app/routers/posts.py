"""
Community Posts API router

Endpoints:
- POST/posts (create a post)
- GET/posts (list posts, with optional filters)
- GET/posts/{post_id} (get single post)
- PATCH/posts/{post_id} (update own post)
- DELETE/posts/{post_id} (delete own post)
"""

from typing import Generator, List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.db import SessionLocal
from app.models import Post, Trail, User
from app import schemas

# adjust path if needed
from app.callback import get_current_user


router = APIRouter(prefix="/posts", tags=["posts"])


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def _to_post_out(post: Post) -> schemas.PostOut:
    # assumes post.user is loaded; SQLAlchemy will lazy-load if needed
    return schemas.PostOut(
        id=post.id,
        user_id=post.user_id,
        trail_id=post.trail_id,
        title=post.title,
        body=post.body,
        created_at=post.created_at,
        updated_at=post.updated_at,
        display_name=post.user.display_name,
    )


@router.post(
    "/",
    response_model=schemas.PostOut,
    status_code=status.HTTP_201_CREATED,
)
def create_post(
    data: schemas.PostCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Create a community post
    Optional trail_id to tie it to a specific trail
    """
    trail_id = data.trail_id

    if trail_id is not None:
        trail = db.get(Trail, trail_id)
        if not trail:
            raise HTTPException(status_code=404, detail="Trail not found")

    post = Post(
        user_id=current_user.id,
        trail_id=trail_id,
        title=data.title,
        body=data.body,
    )

    db.add(post)
    db.commit()
    db.refresh(post)

    return _to_post_out(post)


@router.get(
    "/",
    response_model=List[schemas.PostOut],
)
def list_posts( # filters are optional
    trail_id: Optional[int] = Query(
        default=None,
        description="Filter by trail_id",
    ),
    author_id: Optional[int] = Query(
        default=None,
        description="Filter by user_id",
    ),
    limit: int = Query(
        default=50,
        ge=1,
        le=200,
        description="Max number of posts to return",
    ),
    offset: int = Query(
        default=0,
        ge=0,
        description="Number of posts to skip",
    ),
    db: Session = Depends(get_db),
):
    """
    List community posts, can optionally filter by trail or user
    """
    q = db.query(Post).join(User)

    if trail_id is not None:
        q = q.filter(Post.trail_id == trail_id)

    if author_id is not None:
        q = q.filter(Post.user_id == author_id)

    posts = (
        q.order_by(Post.created_at.desc())
        .offset(offset)
        .limit(limit)
        .all()
    )

    return [_to_post_out(p) for p in posts]


@router.get(
    "/{post_id}",
    response_model=schemas.PostOut,
)
def get_post(
    post_id: int,
    db: Session = Depends(get_db),
):
    """
    Get a single post by id
    """
    post = db.query(Post).filter(Post.id == post_id).first()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")

    return _to_post_out(post)


@router.patch(
    "/{post_id}",
    response_model=schemas.PostOut,
)
def update_post(
    post_id: int,
    data: schemas.PostUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Update a post
    Only the poster can edit their post
    """
    post = db.query(Post).filter(Post.id == post_id).first()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")

    if post.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not allowed to edit this post",
        )

    update_data = data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(post, field, value)

    db.commit()
    db.refresh(post)

    return _to_post_out(post)


@router.delete(
    "/{post_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_post(
    post_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Delete a post
    Only the poster can delete their post
    """
    post = db.query(Post).filter(Post.id == post_id).first()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")

    if post.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not allowed to delete this post",
        )

    db.delete(post)
    db.commit()

    return
