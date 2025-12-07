"""
Quick start script to populate TrailBlazer database with trails
This script will:
1. Import parks from NPS API for several states
2. Add some sample trails to those parks for testing
"""

from app.db import SessionLocal, Base, engine
from app.models import Park, Trail
from app.services.nps import import_parks_by_states

print("=" * 60)
print("TrailBlazer Database Population Script")
print("=" * 60)

# Create tables if they don't exist
Base.metadata.create_all(bind=engine)
db = SessionLocal()

try:
    # Step 1: Import parks from NPS API
    print("\n📡 Importing parks from NPS API...")
    print("-" * 60)

    states = ["NY", "NJ", "NH", "CA", "CO"]
    total_parks = 0

    for state in states:
        try:
            print(f"\n🗺️  Importing {state}...")
            result = import_parks_by_states(db, state)
            total_parks += result.get("total", 0)
            print(f"   ✅ {state}: {result.get('inserted', 0)} new, {result.get('updated', 0)} updated")
        except Exception as e:
            print(f"   ⚠️  {state}: {str(e)}")

    print(f"\n✅ Total parks imported: {total_parks}")

    # Step 2: Add sample trails to make the map more interesting
    print("\n🏔️  Adding sample trails...")
    print("-" * 60)

    # Find Central Park (if it was imported)
    central_park = db.query(Park).filter(
        Park.name.like("%Central%"),
        Park.state == "NY"
    ).first()

    if not central_park:
        # Create Central Park if it doesn't exist
        print("   Creating Central Park...")
        central_park = Park(
            name="Central Park",
            state="NY",
            lat=40.7829,
            lon=-73.9654
        )
        db.add(central_park)
        db.flush()

    # Add trails to Central Park
    sample_trails = [
        Trail(
            park_id=central_park.id,
            name="North Woods Trail",
            difficulty="easy",
            length_km=2.5,
            elevation_gain_m=50,
            lat=40.7989,
            lon=-73.9589,
            accessible=True,
            has_waterfall=False,
            has_viewpoint=True,
            avg_rating=4.5,
            ratings_count=120
        ),
        Trail(
            park_id=central_park.id,
            name="Ramble Trail",
            difficulty="easy",
            length_km=1.8,
            elevation_gain_m=30,
            lat=40.7794,
            lon=-73.9707,
            accessible=True,
            has_waterfall=False,
            has_viewpoint=True,
            avg_rating=4.7,
            ratings_count=200
        ),
        Trail(
            park_id=central_park.id,
            name="Reservoir Loop",
            difficulty="moderate",
            length_km=2.5,
            elevation_gain_m=20,
            lat=40.7859,
            lon=-73.9629,
            accessible=False,
            has_waterfall=False,
            has_viewpoint=True,
            avg_rating=4.6,
            ratings_count=180
        ),
        Trail(
            park_id=central_park.id,
            name="Great Hill Trail",
            difficulty="easy",
            length_km=1.2,
            elevation_gain_m=25,
            lat=40.7997,
            lon=-73.9536,
            accessible=True,
            has_waterfall=False,
            has_viewpoint=True,
            avg_rating=4.4,
            ratings_count=95
        ),
    ]

    # Find Prospect Park
    prospect_park = db.query(Park).filter(
        Park.name.like("%Prospect%"),
        Park.state == "NY"
    ).first()

    if not prospect_park:
        print("   Creating Prospect Park...")
        prospect_park = Park(
            name="Prospect Park",
            state="NY",
            lat=40.6602,
            lon=-73.9690
        )
        db.add(prospect_park)
        db.flush()

    sample_trails.extend([
        Trail(
            park_id=prospect_park.id,
            name="Long Meadow Trail",
            difficulty="easy",
            length_km=3.2,
            elevation_gain_m=40,
            lat=40.6602,
            lon=-73.9690,
            accessible=True,
            has_waterfall=False,
            has_viewpoint=False,
            avg_rating=4.3,
            ratings_count=95
        ),
        Trail(
            park_id=prospect_park.id,
            name="Lake Loop Trail",
            difficulty="easy",
            length_km=5.5,
            elevation_gain_m=60,
            lat=40.6612,
            lon=-73.9700,
            accessible=True,
            has_waterfall=True,
            has_viewpoint=True,
            avg_rating=4.8,
            ratings_count=250
        ),
        Trail(
            park_id=prospect_park.id,
            name="Ravine Trail",
            difficulty="moderate",
            length_km=2.8,
            elevation_gain_m=70,
            lat=40.6590,
            lon=-73.9680,
            accessible=False,
            has_waterfall=True,
            has_viewpoint=False,
            avg_rating=4.6,
            ratings_count=145
        ),
    ])

    # Add trails to Fort Lee if it exists
    fort_lee = db.query(Park).filter(
        Park.name.like("%Fort Lee%"),
        Park.state == "NJ"
    ).first()

    if fort_lee:
        sample_trails.append(
            Trail(
                park_id=fort_lee.id,
                name="Fort Lee Historic Trail",
                difficulty="moderate",
                length_km=1.6,
                elevation_gain_m=80,
                lat=40.8506,
                lon=-73.9535,
                accessible=False,
                has_waterfall=False,
                has_viewpoint=True,
                avg_rating=4.4,
                ratings_count=75
            )
        )

    # Add all sample trails
    for trail in sample_trails:
        db.add(trail)

    db.commit()

    # Final count
    park_count = db.query(Park).count()
    trail_count = db.query(Trail).count()

    print(f"\n✅ Successfully added {len(sample_trails)} sample trails")
    print("\n" + "=" * 60)
    print("📊 DATABASE SUMMARY")
    print("=" * 60)
    print(f"Total Parks:  {park_count}")
    print(f"Total Trails: {trail_count}")

    print("\n🔍 Sample trails you can search for:")
    print("-" * 60)
    for trail in sample_trails:
        print(f"   • {trail.name}")

    print("\n✨ All done! Your database is ready.")
    print("   Restart your backend and app to see the trails!")
    print("=" * 60)

except Exception as e:
    print(f"\n❌ Error: {e}")
    import traceback

    traceback.print_exc()
    db.rollback()
finally:
    db.close()