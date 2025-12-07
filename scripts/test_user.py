"""
Create a test user account for the database
Run this BEFORE add_sample_posts.py
"""

from app.db import SessionLocal
from app.models import User
from passlib.context import CryptContext

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

db = SessionLocal()

try:
    # Check if user already exists
    existing = db.query(User).filter(User.email == "john@example.com").first()

    if existing:
        print("✅ User already exists!")
        print(f"   Name: {existing.display_name}")
        print(f"   Email: {existing.email}")
    else:
        # Create new user
        hashed_password = pwd_context.hash("password123")

        user = User(
            email="john@example.com",
            hashed_password=hashed_password,
            display_name="John Hiker"
        )

        db.add(user)
        db.commit()
        db.refresh(user)

        print("=" * 60)
        print("✅ Test user created successfully!")
        print("=" * 60)
        print(f"Email: {user.email}")
        print(f"Password: password123")
        print(f"Name: {user.display_name}")
        print("\n🎯 You can now:")
        print("   1. Run: python add_sample_posts.py")
        print("   2. Login to the app with these credentials")
        print("=" * 60)

except Exception as e:
    print(f"❌ Error: {e}")
    import traceback

    traceback.print_exc()
    db.rollback()
finally:
    db.close()