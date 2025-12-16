"""
Starts up our database for Trailblazer.

- Creates a SQLAlchemy Engine (SQLite file by default), Alchemy allows python objects to be mapped to the sqllite db 
- Enables useful SQLite PRAGMAs (foreign_keys + WAL(read and write))
- Starts a SessionLocal for our db to run
- Maps our Python classes to db models 
"""

from sqlalchemy import create_engine, event
from sqlalchemy.orm import sessionmaker, declarative_base
import os

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./trailblazer.db")

# SQLite needs this connect arg in multi-threaded apps like FastAPI’s dev server.
connect_args = {"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}


# alchemy engine manages db connections
engine = create_engine(DATABASE_URL, connect_args=connect_args, future=True)



@event.listens_for(engine, "connect")
def _set_sqlite_pragmas(dbapi_connection, connection_record):
    try:
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA foreign_keys=ON") # makes sure we always have foreign keys where foreign keys are needed
        cursor.execute("PRAGMA journal_mode=WAL")  # can read and write to/from the database concurrently
        cursor.close()
    except Exception:
        # If not SQLite then we can ignore.
        pass


# each request should create its own session
SessionLocal = sessionmaker(
    bind=engine,
    autocommit=False,
    autoflush=False,
    future=True,
)


# All ORM models should inherit from this Base:
Base = declarative_base()
