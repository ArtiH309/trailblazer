"""
NPS Admin API router

Endpoint:
POST /admin/nps/refresh

Triggers a sync with the NPS API to insert parks into the db
using the import_parks_by_states function from services/nps -> scripts/import_nps
"""

from typing import Generator, Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.db import SessionLocal
from app.models import User
from app import schemas
from app.services.nps import import_parks_by_states


from app.callback import get_current_user


router = APIRouter(prefix="/admin/nps", tags=["nps-admin"])


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def ensure_admin(user: User) -> None:
    """
    Ensures the user is authenticated
    """

    return


@router.post(
    "/refresh",
    response_model=schemas.NpsImportOut,
    status_code=status.HTTP_200_OK,
)
def refresh_parks_from_nps(
    state_code: Optional[str] = Query(
        default=None,
        description="Optional state code. If left out then, it may handle multiple states",
        examples=["NY", "NH", "NJ"],
    ),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Trigger an NPS import using the import_parks_by_states function
    """
    ensure_admin(current_user)

    if not state_code:
        raise HTTPException(
            status_code=400,
            detail="state_code is required",
        )

    state = state_code.upper()

    try:
        result = import_parks_by_states(db, state)
        # result should be like: {"inserted": 32, "updated": 0, "total": 32}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"NPS import failed: {e}")

    return schemas.NpsImportOut(
        ok=True,
        state=state,
        inserted=result.get("inserted", 0),
        updated=result.get("updated", 0),
        total=result.get("total", 0),
    )
