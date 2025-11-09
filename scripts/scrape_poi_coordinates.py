#!/usr/bin/env python3
"""
Scrape POI coordinates from camidecavalls.com interactive map.
Extracts the correct latitude/longitude for all POIs.
"""

import json
import re
from urllib.request import Request, urlopen

def scrape_map_coordinates():
    """
    Scrape coordinates from the interactive map JavaScript.
    The map uses WKT format: POINT(longitude latitude)
    """
    url = 'https://www.camidecavalls.com/Mapa.aspx'

    headers = {
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
    }

    req = Request(url, headers=headers)
    response = urlopen(req, timeout=15)
    html = response.read().decode('utf-8', errors='ignore')

    # Extract all POI coordinate patterns
    # Looking for: wktFormat.readFeature("POINT(lon lat)")
    # and: feature.setId('feature####');

    coordinates = {}

    # Pattern to find POINT coordinates
    point_pattern = r'wktFormat\.readFeature\("POINT\(([0-9.]+)\s+([0-9.]+)\)"'
    # Pattern to find feature IDs
    id_pattern = r'feature\.setId\(\'feature(\d+)\'\)'

    # Find all matches
    points = re.findall(point_pattern, html)
    ids = re.findall(id_pattern, html)

    print(f"Found {len(points)} coordinate points")
    print(f"Found {len(ids)} feature IDs")

    if len(points) == len(ids):
        for i, (lon, lat) in enumerate(points):
            poi_id = ids[i]
            coordinates[poi_id] = {
                'latitude': float(lat),
                'longitude': float(lon)
            }
            print(f"POI {poi_id}: {lat}, {lon}")
    else:
        print("⚠️  Warning: Number of points and IDs don't match!")
        print("    This might indicate the parsing pattern needs adjustment")

    return coordinates


if __name__ == '__main__':
    print("🗺️  Camí de Cavalls - POI Coordinates Scraper")
    print("=" * 50)

    try:
        coords = scrape_map_coordinates()

        # Save to JSON
        output_file = 'scripts/camidecavalls_pois/coordinates_from_map.json'
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(coords, f, ensure_ascii=False, indent=2)

        print(f"\n✅ Saved {len(coords)} POI coordinates to {output_file}")

        # Show specific POIs we're interested in
        print("\n🔍 POIs of interest:")
        for poi_id in ['9664', '9665', '9666']:
            if poi_id in coords:
                print(f"  POI {poi_id}: {coords[poi_id]}")
            else:
                print(f"  POI {poi_id}: NOT FOUND in map")

    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()
