"""
Add sample community posts to the database for testing
Run this after you've created at least one user account
"""

from app.db import SessionLocal, Base, engine
from app.models import User, Trail, Post
from datetime import datetime, timedelta

Base.metadata.create_all(bind=engine)
db = SessionLocal()

try:
    # Get first user (or create a sample user)
    user = db.query(User).first()

    if not user:
        print("❌ No users found! Please register a user in the app first.")
        print("   Go to the app → Register → Create an account")
        print("   Then run this script again.")
        exit(1)

    # Get some trails
    trails = db.query(Trail).limit(5).all()

    if not trails:
        print("❌ No trails found! Please run populate_database.py first.")
        exit(1)

    # Sample posts data
    sample_posts = [
        {
            "body": "Just completed the North Woods Trail! The fall colors are absolutely stunning right now. Highly recommend for a peaceful afternoon hike. 🍂",
            "trail_id": trails[0].id if len(trails) > 0 else None
        },
        {
            "body": "Morning hike at Ramble Trail was amazing! Saw a family of deer and the sunrise was incredible. Perfect way to start the day! 🦌☀️",
            "trail_id": trails[1].id if len(trails) > 1 else None
        },
        {
            "body": "The Reservoir Loop is challenging but worth it! The views from the top are breathtaking. Bring plenty of water! 💧",
            "trail_id": trails[2].id if len(trails) > 2 else None
        },
        {
            "body": "Anyone up for a group hike this weekend? Looking to explore some new trails in the area. Comment if interested! 🥾",
            "trail_id": None
        },
        {
            "body": "Pro tip: Download trails for offline use before heading out. Saved me when I lost signal halfway through my hike today! 📱",
            "trail_id": None
        },
        {
            "body": "Just hit 100 miles total! Thanks to this app for keeping me motivated. What's your total distance? 🎉",
            "trail_id": None
        },
        {
            "body": "The Lake Loop Trail is perfect for beginners! Not too challenging and beautiful scenery. Great for a family outing. 👨‍👩‍👧‍👦",
            "trail_id": trails[3].id if len(trails) > 3 else None
        },
        {
            "body": "Trail conditions update: Ramble Trail has some muddy sections after yesterday's rain. Wear appropriate footwear! 👟",
            "trail_id": trails[1].id if len(trails) > 1 else None
        },
        {
            "body": "Sunrise hike at Great Hill was magical this morning! Got there at 6am and had the whole trail to myself. 🌅",
            "trail_id": trails[4].id if len(trails) > 4 else None
        },
        {
            "body": "Looking for trail buddies! New to the area and would love to explore with some fellow hikers. Drop me a message! 👋",
            "trail_id": None
        }
    ]

    # Add posts with varying timestamps
    added_posts = []
    base_time = datetime.utcnow()

    for i, post_data in enumerate(sample_posts):
        # Create posts at different times (most recent first)
        created_time = base_time - timedelta(hours=i * 3)

        post = Post(
            user_id=user.id,
            trail_id=post_data["trail_id"],
            title=None,  # Optional title
            body=post_data["body"],
            created_at=created_time,
            updated_at=created_time
        )
        db.add(post)
        added_posts.append(post)

    db.commit()

    print("=" * 60)
    print("✅ Successfully added sample community posts!")
    print("=" * 60)
    print(f"Total posts created: {len(added_posts)}")
    print(f"Posted by: {user.display_name} ({user.email})")
    print("\n📝 Sample posts:")
    print("-" * 60)

    for post in added_posts[:3]:  # Show first 3 as preview
        preview = post.body[:60] + "..." if len(post.body) > 60 else post.body
        print(f"   • {preview}")

    print(f"\n   ... and {len(added_posts) - 3} more posts!")
    print("\n✨ Open the Community tab in your app to see them!")
    print("=" * 60)

except Exception as e:
    print(f"❌ Error: {e}")
    import traceback
    traceback.print_exc()
    db.rollback()
finally:
    db.close()