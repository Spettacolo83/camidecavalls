#!/usr/bin/env python3
"""
Script to add elevation data to route coordinates using Open-Elevation API.

This script:
1. Reads GeoJSON data from RouteData.kt
2. Extracts coordinates for each route
3. Calls Open-Elevation API to get elevation for each point
4. Updates coordinates to [lon, lat, elevation] format
5. Generates updated RouteData.kt with elevation data
"""

import json
import re
import requests
import time
from typing import List, Tuple

# Open-Elevation API endpoint
ELEVATION_API = "https://api.open-elevation.com/api/v1/lookup"

# Batch size for API requests (API supports up to 1000 points per request)
BATCH_SIZE = 100

def extract_routes_from_kotlin(file_path: str) -> dict:
    """
    Extract route data from RouteData.kt file.
    Returns dict mapping route_number -> geojson_string
    """
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    routes = {}

    # Pattern to match route number and gpxData
    pattern = r'Route\(\s*id\s*=\s*(\d+).*?gpxData\s*=\s*"""(.*?)"""\s*\.trimIndent\(\)'

    matches = re.findall(pattern, content, re.DOTALL)

    for route_id, geojson_str in matches:
        # Clean up the GeoJSON string
        geojson_str = geojson_str.strip()
        routes[int(route_id)] = geojson_str

    print(f"✅ Extracted {len(routes)} routes from RouteData.kt")
    return routes

def get_elevation_for_coordinates(coordinates: List[Tuple[float, float]]) -> List[float]:
    """
    Get elevation data from Open-Elevation API for a list of coordinates.

    Args:
        coordinates: List of (longitude, latitude) tuples

    Returns:
        List of elevation values in meters
    """
    elevations = []

    # Process in batches
    for i in range(0, len(coordinates), BATCH_SIZE):
        batch = coordinates[i:i + BATCH_SIZE]

        # Convert to API format: {"latitude": lat, "longitude": lon}
        locations = [{"latitude": lat, "longitude": lon} for lon, lat in batch]

        print(f"  Fetching elevation for points {i+1} to {i+len(batch)}...")

        try:
            # Make API request
            response = requests.post(
                ELEVATION_API,
                json={"locations": locations},
                headers={"Accept": "application/json"},
                timeout=30
            )

            if response.status_code == 200:
                data = response.json()
                batch_elevations = [result["elevation"] for result in data["results"]]
                elevations.extend(batch_elevations)

                # Be nice to the API - small delay between requests
                if i + BATCH_SIZE < len(coordinates):
                    time.sleep(0.5)
            else:
                print(f"  ⚠️  API error: {response.status_code}")
                # Return zeros on error
                elevations.extend([0.0] * len(batch))

        except Exception as e:
            print(f"  ⚠️  Request error: {e}")
            # Return zeros on error
            elevations.extend([0.0] * len(batch))

    return elevations

def add_elevation_to_route(route_num: int, geojson_str: str) -> str:
    """
    Add elevation data to a route's GeoJSON coordinates.

    Args:
        route_num: Route number
        geojson_str: GeoJSON string with [lon, lat] coordinates

    Returns:
        Updated GeoJSON string with [lon, lat, elevation] coordinates
    """
    print(f"\n🗺️  Processing Route {route_num}...")

    try:
        # Parse GeoJSON
        geojson = json.loads(geojson_str)

        # Extract coordinates
        coordinates = geojson["coordinates"]
        print(f"  📍 Found {len(coordinates)} points")

        # Get elevation data
        elevations = get_elevation_for_coordinates(coordinates)

        # Add elevation to coordinates
        updated_coordinates = []
        for (lon, lat), ele in zip(coordinates, elevations):
            updated_coordinates.append([lon, lat, round(ele, 1)])

        # Update GeoJSON
        geojson["coordinates"] = updated_coordinates

        # Calculate elevation stats
        min_ele = min(elevations)
        max_ele = max(elevations)
        print(f"  📊 Elevation range: {min_ele:.1f}m - {max_ele:.1f}m")

        # Convert back to JSON string
        updated_geojson_str = json.dumps(geojson, separators=(',', ':'))

        return updated_geojson_str

    except Exception as e:
        print(f"  ❌ Error processing route: {e}")
        return geojson_str

def update_kotlin_file(file_path: str, updated_routes: dict) -> None:
    """
    Update RouteData.kt with new GeoJSON data containing elevation.

    Args:
        file_path: Path to RouteData.kt
        updated_routes: Dict mapping route_number -> updated_geojson_string
    """
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replace each route's gpxData
    for route_num, new_geojson in sorted(updated_routes.items()):
        # Pattern to match this specific route's gpxData
        pattern = rf'(Route\(\s*id\s*=\s*{route_num}.*?gpxData\s*=\s*""").*?("""\s*\.trimIndent\(\))'

        # Replacement with new GeoJSON
        replacement = rf'\1{new_geojson}\2'

        content = re.sub(pattern, replacement, content, flags=re.DOTALL)

    # Write updated content
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"\n✅ Updated RouteData.kt with elevation data for {len(updated_routes)} routes")

def main():
    """Main function to add elevation data to all routes."""
    import sys

    print("🚀 Starting elevation data addition...")
    print("=" * 60)

    # File path
    route_data_path = "../composeApp/src/commonMain/kotlin/com/followmemobile/camidecavalls/data/RouteData.kt"

    # Extract routes
    routes = extract_routes_from_kotlin(route_data_path)

    if not routes:
        print("❌ No routes found in RouteData.kt")
        return

    # Check if --all flag is provided
    process_all = "--all" in sys.argv

    if not process_all:
        # Process only Route 1 first as a test
        print("\n🧪 TEST MODE: Processing only Route 1...")
        test_route_num = 1

        if test_route_num not in routes:
            print(f"❌ Route {test_route_num} not found")
            return

        # Add elevation to test route
        updated_geojson = add_elevation_to_route(test_route_num, routes[test_route_num])

        # Save result to a test file
        test_output = f"route_{test_route_num}_with_elevation.json"
        with open(test_output, 'w') as f:
            json.dump(json.loads(updated_geojson), f, indent=2)

        print(f"\n✅ Test complete! Result saved to {test_output}")
        print("\n📝 Next steps:")
        print("  1. Review the test file to verify elevation data")
        print("  2. If correct, run script with --all flag to process all routes")
        print("  3. Script will update RouteData.kt automatically")
    else:
        # Process all routes
        print("\n🚀 FULL MODE: Processing all 20 routes...")
        print("This will take several minutes due to API rate limits.\n")

        updated_routes = {}

        for route_num in sorted(routes.keys()):
            try:
                updated_geojson = add_elevation_to_route(route_num, routes[route_num])
                updated_routes[route_num] = updated_geojson

                # Small delay between routes to be nice to the API
                if route_num < 20:
                    time.sleep(1)

            except Exception as e:
                print(f"  ❌ Failed to process route {route_num}: {e}")
                continue

        if updated_routes:
            print("\n💾 Updating RouteData.kt...")
            update_kotlin_file(route_data_path, updated_routes)
            print("\n✅ All routes updated with elevation data!")
            print("\n📝 Next steps:")
            print("  1. Review the changes in RouteData.kt")
            print("  2. Build and test the app")
            print("  3. Commit the changes")
        else:
            print("\n❌ No routes were successfully updated")

if __name__ == "__main__":
    main()
