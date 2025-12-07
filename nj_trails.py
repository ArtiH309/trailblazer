"""
Add New Jersey area trails so markers show up on the map
Run this to populate trails in NJ region
"""

from app.db import SessionLocal
from app.models import Park, Trail
from datetime import datetime

db = SessionLocal()

try:
    print("=" * 60)
    print("Adding New Jersey Area Trails")
    print("=" * 60)

    # Find or create parks in NJ
    parks_data = [
        {
            "name": "Liberty State Park",
            "state": "NJ",
            "lat": 40.7056,
            "lon": -74.0564
        },
        {
            "name": "High Point State Park",
            "state": "NJ",
            "lat": 41.3211,
            "lon": -74.6620
        },
        {
            "name": "Delaware Water Gap",
            "state": "NJ",
            "lat": 41.0026,
            "lon": -75.1333
        },
        {
            "name": "Palisades Interstate Park",
            "state": "NJ",
            "lat": 40.9487,
            "lon": -73.9082
        },
        {
            "name": "Wharton State Forest",
            "state": "NJ",
            "lat": 39.7626,
            "lon": -74.5865
        }
    ]

    parks = []
    for park_data in parks_data:
        park = db.query(Park).filter(
            Park.name == park_data["name"],
            Park.state == park_data["state"]
        ).first()

        if not park:
            park = Park(**park_data)
            db.add(park)
            db.flush()
            print(f"✅ Created park: {park.name}")
        else:
            print(f"📍 Found existing park: {park.name}")

        parks.append(park)

    db.commit()

    # Add trails to each park
    trails_data = [
        # Liberty State Park trails
        {
            "park_id": parks[0].id,
            "name": "Liberty Walk Trail",
            "difficulty": "easy",
            "length_km": 3.2,
            "elevation_gain_m": 10,
            "lat": 40.7056,
            "lon": -74.0564,
            "accessible": True,
            "has_waterfall": False,
            "has_viewpoint": True,
            "avg_rating": 4.5,
            "ratings_count": 180
        },
        {
            "park_id": parks[0].id,
            "name": "Hudson River Walk",
            "difficulty": "easy",
            "length_km": 2.4,
            "elevation_gain_m": 5,
            "lat": 40.7080,
            "lon": -74.0520,
            "accessible": True,
            "has_waterfall": False,
            "has_viewpoint": True,
            "avg_rating": 4.7,
            "ratings_count": 250
        },

        # High Point State Park trails
        {
            "park_id": parks[1].id,
            "name": "Monument Trail",
            "difficulty": "moderate",
            "length_km": 5.6,
            "elevation_gain_m": 250,
            "lat": 41.3211,
            "lon": -74.6620,
            "accessible": False,
            "has_waterfall": False,
            "has_viewpoint": True,
            "avg_rating": 4.8,
            "ratings_count": 320
        },
        {
            "park_id": parks[1].id,
            "name": "Appalachian Trail Section",
            "difficulty": "hard",
            "length_km": 8.9,
            "elevation_gain_m": 400,
            "lat": 41.3150,
            "lon": -74.6680,
            "accessible": False,
            "has_waterfall": False,
            "has_viewpoint": True,
            "avg_rating": 4.9,
            "ratings_count": 425
        },

        # Delaware Water Gap trails
        {
            "park_id": parks[2].id,
            "name": "Mount Tammany Trail",
            "difficulty": "hard",
            "length_km": 6.5,
            "elevation_gain_m": 380,
            "lat": 40.9656,
            "lon": -75.1264,
            "accessible": False,
            "has_waterfall": False,
            "has_viewpoint": True,
            "avg_rating": 4.9,
            "ratings_count": 580
        },
        {
            "park_id": parks[2].id,
            "name": "Dunnfield Creek Trail",
            "difficulty": "moderate",
            "length_km": 4.8,
            "elevation_gain_m": 180,
            "lat": 40.9580,
            "lon": -75.1310,
            "accessible": False,
            "has_waterfall": True,
            "has_viewpoint": False,
            "avg_rating": 4.6,
            "ratings_count": 290
        },

        # Palisades trails
        {
            "park_id": parks[3].id,
            "name": "Shore Trail",
            "difficulty": "easy",
            "length_km": 19.3,
            "elevation_gain_m": 150,
            "lat": 40.9487,
            "lon": -73.9082,
            "accessible": False,
            "has_waterfall": False,
            "has_viewpoint": True,
            "avg_rating": 4.7,
            "ratings_count": 410
        },
        {
            "park_id": parks[3].id,
            "name": "Long Path",
            "difficulty": "moderate",
            "length_km": 11.2,
            "elevation_gain_m": 220,
            "lat": 40.9520,
            "lon": -73.9120,
            "accessible": False,
            "has_waterfall": False,
            "has_viewpoint": True,
            "avg_rating": 4.6,
            "ratings_count": 340
        },

        # Wharton State Forest trails
        {
            "park_id": parks[4].id,
            "name": "Batona Trail",
            "difficulty": "moderate",
            "length_km": 80.5,
            "elevation_gain_m": 100,
            "lat": 39.7626,
            "lon": -74.5865,
            "accessible": False,
            "has_waterfall": False,
            "has_viewpoint": False,
            "avg_rating": 4.5,
            "ratings_count": 215
        },
        {
            "park_id": parks[4].id,
            "name": "Mullica River Trail",
            "difficulty": "easy",
            "length_km": 3.7,
            "elevation_gain_m": 15,
            "lat": 39.7580,
            "lon": -74.5920,
            "accessible": True,
            "has_waterfall": False,
            "has_viewpoint": False,
            "avg_rating": 4.3,
            "ratings_count": 145
        }
    ]

    for trail_data in trails_data:
        # Check if trail already exists
        existing = db.query(Trail).filter(
            Trail.name == trail_data["name"],
            Trail.park_id == trail_data["park_id"]
        ).first()

        if not existing:
            trail = Trail(**trail_data)
            db.add(trail)
            print(f"  ✅ Added trail: {trail.name}")
        else:
            print(f"  📍 Trail already exists: {trail_data['name']}")

    db.commit()

    # Final count
    total_trails = db.query(Trail).count()
    nj_trails = db.query(Trail).join(Park).filter(Park.state == "NJ").count()

    print("\n" + "=" * 60)
    print("📊 DATABASE SUMMARY")
    print("=" * 60)
    print(f"Total Trails in Database: {total_trails}")
    print(f"New Jersey Trails: {nj_trails}")

    print("\n🗺️  Trail Locations:")
    print("-" * 60)
    for trail in db.query(Trail).join(Park).filter(Park.state == "NJ").limit(10).all():
        print(f"   📍 {trail.name} ({trail.lat:.4f}, {trail.lon:.4f})")

    print("\n✨ All done! NJ trails are ready!")
    print("   Restart your app to see the markers!")
    print("=" * 60)

except Exception as e:
    print(f"\n❌ Error: {e}")
    import traceback

    traceback.print_exc()
    db.rollback()
finally:
    db.close()
