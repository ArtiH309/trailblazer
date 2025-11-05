"""
Uses the NPS API to fetch parks and store them in the parks table.
"""

from typing import Dict
import httpx # makes web requests
from sqlalchemy.orm import Session
from app.config import settings
from app.models import Park

endpoint = "https://developer.nps.gov/api/v1/parks" # http header from the nps website

def import_parks_by_states(db: Session, state_code: str, limit: int = 500) -> Dict[str, int]: # fetches parks that are given by the state code, "NY" and then fills up the table in parks with them, up to 500 as i put here
    if not settings.NPS_API_KEY:
        raise RuntimeError("Remember to set the NPS API Key")
    

    params = { # this requests the fields we want from the API, and builds the query string to the API url
        "stateCode": state_code.upper(),
        "limit": limit,
        "api_key": settings.NPS_API_KEY
    }

    with httpx.Client(timeout = 30.0) as client:
        response = client.get(endpoint, params=params) # sends request to NPS
        response.raise_for_status()
        data = response.json().get("data", []) # the nps data is stored as a json, so we parse througn the data json

    inserted = 0 # will tell us how many parks were inserted into the table
    updated = 0 # how the table got updated

    for item in data:
        nps_id = item.get("id") # the park id
        name = item.get("name") or "Unnamed Park"
        lat = float(item["latitude"]) if item.get("latitude") or item.get("lat") else None
        lon = float(item["longitude"]) if item.get("longitude") or item.get("long") else None

        park = db.query(Park).filter((Park.nps_id == nps_id) | ((Park.name == name) & (Park.state == state_code.upper()))).first()
        # the query on the Park table: filter it by id or name/state and find a match with first(). SO if park exists, update the existing row -- if none, then we insert a new row

        if park: # if we update a park
            park.nps_id = nps_id
            park.name = name
            park.state = state_code.upper()
            park.lat = lat
            park.lon = lon
            updated+=1 # a counter for how many was updated
        else: # if a new park is inserted
            park = Park(
                nps_id = nps_id,
                name = name,
                state = state_code.upper(),
                lat = lat,
                lon = lon
            )
            db.add(park)
            inserted +=1 # counter for how many was inserted

    db.commit()
    total = inserted + updated
    return {"inserted": inserted, "updated": updated, "total": total} # sum slight to tell us what happened
