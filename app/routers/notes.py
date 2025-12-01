"""
Notes API router

Endpoints:
- GET/trails/{trail_id}/notes (list current user's notes for a trail)
- POST/trails/{trail_id}/notes (create a note for a trail)
- PATCH/notes/{note_id} (update a note)
- DELETE/notes/{note_id} (delete a note)

Notes are private to each user. You can only see/edit/delete your own notes
"""

from typing import Generator, List

from fastapi import APIRouter, Depends, HTTPException, status, Response
from sqlalchemy.orm import Session

from app.db import SessionLocal
from app.models import Note, Trail, User
from app import schemas

from app.callback import get_current_user


router = APIRouter(prefix="", tags=["notes"])


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.get(
    "/trails/{trail_id}/notes",
    response_model=List[schemas.NoteOut],
)
def list_notes_for_trail(
    trail_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    List the current user notes for a specific trail
    Notes are private so you only see your own notes for that trail
    """
    trail = db.get(Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Trail not found")

    notes = (
        db.query(Note)
        .filter(
            Note.trail_id == trail_id,
            Note.user_id == current_user.id,
        )
        .order_by(
            Note.is_pinned.desc(), # pinned notes first
            Note.created_at.desc(), # newest first
        )
        .all()
    )
    return notes


@router.post(
    "/trails/{trail_id}/notes",
    response_model=schemas.NoteOut,
    status_code=status.HTTP_201_CREATED,
)
def create_note_for_trail(
    trail_id: int,
    note_in: schemas.NoteCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Create a new note for a trail
    """
    trail = db.get(Trail, trail_id)
    if not trail:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Trail not found")

    note = Note(
        trail_id=trail_id,
        user_id=current_user.id,
        text=note_in.text,
        is_pinned=note_in.is_pinned,
    )

    db.add(note)
    db.commit()
    db.refresh(note)

    return note


@router.patch(
    "/notes/{note_id}",
    response_model=schemas.NoteOut,
)
def update_note(
    note_id: int,
    note_in: schemas.NoteUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Update an existing note
    Only the user of the note can update it
    """
    note = db.query(Note).filter(Note.id == note_id).first()
    if not note:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Note not found")

    if note.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not allowed to edit this note",
        )

    update_data = note_in.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(note, field, value)

    db.commit()
    db.refresh(note)

    return note


@router.delete(
    "/notes/{note_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_note(
    note_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Delete a note
    Only the user of the note can delete it
    """
    note = db.query(Note).filter(Note.id == note_id).first()
    if not note:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Note not found")

    if note.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not allowed to delete this note",
        )

    db.delete(note)
    db.commit()

    return Response(status_code=status.HTTP_204_NO_CONTENT)
