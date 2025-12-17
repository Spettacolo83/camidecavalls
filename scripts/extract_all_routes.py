#!/usr/bin/env python3
"""
Automated elevation profile extraction from official camidecavalls.com images.

Usage:
    python3 extract_all_routes.py <route_number> [--update]
    python3 extract_all_routes.py all [--update]

The script:
1. Downloads the profile image from https://www.camidecavalls.com/Imas/General/perfil{N}d.png
2. Extracts the elevation profile by finding visible pixels
3. Maps X coordinates to distance (km)
4. Maps Y coordinates to elevation (m)
5. Optionally updates RouteData.kt with --update flag
"""

import os
import sys
import json
import math
import re
import urllib.request
from PIL import Image

# Route metadata: (distance_km, min_elevation, max_elevation)
# These values come from the official website or RouteData.kt
ROUTE_DATA = {
    1: {"name": "Maó - Es Grau", "distance": 10.1, "min_elev": 5, "max_elev": 89},
    2: {"name": "Es Grau - Favàritx", "distance": 8.63, "min_elev": 5, "max_elev": 47},
    3: {"name": "Favàritx - Arenal d'en Castell", "distance": 13.60, "min_elev": 0, "max_elev": 78},
    4: {"name": "Arenal d'en Castell - Cala Tirant", "distance": 10.77, "min_elev": 3, "max_elev": 38},
    5: {"name": "Son Parc - Fornells", "distance": 9.59, "min_elev": 1, "max_elev": 47},
    6: {"name": "Fornells - Cala Tirant", "distance": 8.61, "min_elev": 2, "max_elev": 118},
    7: {"name": "Cala Tirant - Binimel·là", "distance": 11.2, "min_elev": 0, "max_elev": 95},
    8: {"name": "Binimel·là - Els Alocs", "distance": 7.3, "min_elev": 0, "max_elev": 68},
    9: {"name": "Els Alocs - Algaiarens", "distance": 7.0, "min_elev": 0, "max_elev": 80},
    10: {"name": "Algaiarens - Cala Morell", "distance": 8.5, "min_elev": 0, "max_elev": 70},
    11: {"name": "Cala Morell - Ciutadella", "distance": 10.5, "min_elev": 0, "max_elev": 35},
    12: {"name": "Ciutadella - Cap d'Artrutx", "distance": 13.0, "min_elev": 0, "max_elev": 25},
    13: {"name": "Cap d'Artrutx - Cala en Turqueta", "distance": 13.5, "min_elev": 0, "max_elev": 45},
    14: {"name": "Cala en Turqueta - Cala Galdana", "distance": 8.0, "min_elev": 0, "max_elev": 60},
    15: {"name": "Cala Galdana - Sant Tomàs", "distance": 11.5, "min_elev": 0, "max_elev": 70},
    16: {"name": "Sant Tomàs - Son Bou", "distance": 6.5, "min_elev": 0, "max_elev": 55},
    17: {"name": "Son Bou - Cala en Porter", "distance": 8.0, "min_elev": 0, "max_elev": 75},
    18: {"name": "Cala en Porter - Binissafúller", "distance": 8.5, "min_elev": 0, "max_elev": 60},
    19: {"name": "Binissafúller - Punta Prima", "distance": 7.0, "min_elev": 0, "max_elev": 40},
    20: {"name": "Punta Prima - Maó", "distance": 12.5, "min_elev": 0, "max_elev": 50},
}

SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.dirname(SCRIPTS_DIR)


def download_profile_image(route_num: int) -> str:
    """Download the profile image for a route."""
    url = f"https://www.camidecavalls.com/Imas/General/perfil{route_num}d.png"
    local_path = os.path.join(SCRIPTS_DIR, f"perfil{route_num}d.png")

    if not os.path.exists(local_path):
        print(f"Downloading {url}...")
        urllib.request.urlretrieve(url, local_path)
    else:
        print(f"Using cached {local_path}")

    return local_path


def is_profile_pixel(r, g, b, a):
    """Check if pixel is part of the profile (line or colored fill), not white grid lines."""
    if a < 100:
        return False

    # White/light gray pixels are grid lines - ignore
    if r > 240 and g > 240 and b > 240:
        return False

    # Dark pixels are the profile line
    if r < 100 and g < 100 and b < 100:
        return True

    # Green fill: approximately (139, 232, 125)
    if g > r and g > b and g > 150:
        return True

    # Orange/tan fill: approximately (237, 199, 114)
    if r > 180 and g > 150 and b < 180 and r > b:
        return True

    # Gray dotted lines - ignore (they have similar R, G, B values)
    if abs(r - g) < 20 and abs(g - b) < 20 and r > 150:
        return False

    return True


def extract_profile(image_path: str, route_num: int) -> list:
    """Extract elevation profile from image."""
    img = Image.open(image_path)
    route_info = ROUTE_DATA[route_num]

    print(f"Image: {img.width} x {img.height}")
    print(f"Route {route_num}: {route_info['name']}")
    print(f"Distance: {route_info['distance']}km, Elevation: {route_info['min_elev']}m - {route_info['max_elev']}m")

    # Extract raw Y values for each X - look for profile pixels (not white grid lines)
    raw_profile = []
    for x in range(img.width):
        for y in range(img.height):
            r, g, b, a = img.getpixel((x, y))
            if is_profile_pixel(r, g, b, a):
                raw_profile.append((x, y))
                break
        else:
            # No profile pixel found in this column
            if raw_profile:
                raw_profile.append((x, raw_profile[-1][1]))

    # Find actual profile boundaries (exclude edge artifacts)
    # Look for where the profile stabilizes (not jumping wildly)
    ys = [p[1] for p in raw_profile]

    # Find start: first point where it doesn't jump more than 50 pixels in next 5 points
    start_x = 0
    for i in range(len(ys) - 5):
        if all(abs(ys[i] - ys[i+j]) < 50 for j in range(1, 6)):
            start_x = i
            break

    # Find end: last point where it doesn't jump more than 50 pixels in previous 5 points
    end_x = len(ys) - 1
    for i in range(len(ys) - 1, 5, -1):
        if all(abs(ys[i] - ys[i-j]) < 50 for j in range(1, 6)):
            end_x = i
            break

    # Trim profile
    trimmed = raw_profile[start_x:end_x+1]
    print(f"Profile boundaries: x={start_x} to x={end_x} ({len(trimmed)} points)")

    # Find Y range for calibration
    ys_trimmed = [p[1] for p in trimmed]
    y_min = min(ys_trimmed)  # Highest elevation (top of image)
    y_max = max(ys_trimmed)  # Lowest elevation (bottom of image)

    print(f"Y range: {y_min} (max elev) to {y_max} (min elev)")

    # Convert to km and elevation
    profile = []
    x_range = end_x - start_x
    y_range = y_max - y_min

    min_elev = route_info['min_elev']
    max_elev = route_info['max_elev']
    elev_range = max_elev - min_elev
    distance_km = route_info['distance']

    for i, (x, y) in enumerate(trimmed):
        # Map X to km
        km = (i / x_range) * distance_km if x_range > 0 else 0

        # Map Y to elevation (inverted: lower Y = higher elevation)
        if y_range > 0:
            elev = max_elev - ((y - y_min) / y_range) * elev_range
        else:
            elev = max_elev

        elev = max(min_elev, min(max_elev, elev))
        profile.append((round(km, 3), round(elev, 1)))

    return profile


def sample_profile(profile: list, target_points: int = 200) -> list:
    """Sample profile to reduce number of points."""
    if len(profile) <= target_points:
        return profile

    step = len(profile) / target_points
    sampled = []
    for i in range(target_points):
        idx = int(i * step)
        sampled.append(profile[idx])

    # Always include the last point
    if profile[-1] not in sampled:
        sampled.append(profile[-1])

    return sampled


def save_profile(profile: list, route_num: int):
    """Save profile to JSON file."""
    route_info = ROUTE_DATA[route_num]

    output = {
        "route": route_num,
        "name": route_info['name'],
        "distance_km": route_info['distance'],
        "min_elev": route_info['min_elev'],
        "max_elev": route_info['max_elev'],
        "points": profile
    }

    output_path = os.path.join(SCRIPTS_DIR, f"route{route_num}_profile.json")
    with open(output_path, 'w') as f:
        json.dump(output, f, indent=2)

    print(f"Saved to {output_path}")
    return output_path


def print_profile_summary(profile: list, route_num: int):
    """Print a summary of the extracted profile."""
    route_info = ROUTE_DATA[route_num]

    elevs = [p[1] for p in profile]
    print(f"\n--- Profile Summary ---")
    print(f"Points: {len(profile)}")
    print(f"Elevation range: {min(elevs):.1f}m - {max(elevs):.1f}m")

    # Find peak
    peak_idx = elevs.index(max(elevs))
    print(f"Peak: {profile[peak_idx][0]:.2f}km at {profile[peak_idx][1]:.1f}m")

    # Key points
    print("\nKey elevations:")
    km_targets = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, route_info['distance']]
    for target in km_targets:
        if target <= route_info['distance']:
            closest = min(profile, key=lambda p: abs(p[0] - target))
            print(f"  {closest[0]:.1f}km: {closest[1]:.1f}m")


def haversine_distance(lon1, lat1, lon2, lat2):
    """Calculate distance between two coordinates in km."""
    R = 6371
    lat1_rad = math.radians(lat1)
    lat2_rad = math.radians(lat2)
    delta_lat = math.radians(lat2 - lat1)
    delta_lon = math.radians(lon2 - lon1)

    a = math.sin(delta_lat/2)**2 + math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(delta_lon/2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    return R * c


def interpolate_elevation(km, profile):
    """Interpolate elevation at a given km position."""
    if km <= profile[0][0]:
        return profile[0][1]
    if km >= profile[-1][0]:
        return profile[-1][1]

    for i in range(len(profile) - 1):
        if profile[i][0] <= km <= profile[i+1][0]:
            km1, elev1 = profile[i]
            km2, elev2 = profile[i+1]
            if km2 == km1:
                return elev1
            t = (km - km1) / (km2 - km1)
            return elev1 + t * (elev2 - elev1)

    return profile[-1][1]


def update_route_data(profile: list, route_num: int):
    """Update RouteData.kt with the new elevation profile."""
    routedata_path = os.path.join(
        PROJECT_DIR,
        "composeApp/src/commonMain/kotlin/com/followmemobile/camidecavalls/data/RouteData.kt"
    )

    with open(routedata_path, 'r') as f:
        content = f.read()

    # Find the route's gpxData
    next_route = route_num + 1
    if route_num == 20:
        # Last route - different pattern
        pattern = rf'(id = {route_num},.*?gpxData = """)(.*?)(""".trimIndent\(\)\s*\)\s*\))'
    else:
        pattern = rf'(id = {route_num},.*?gpxData = """)(.*?)(""".trimIndent\(\)\s*\),\s*Route\(\s*id = {next_route})'

    match = re.search(pattern, content, re.DOTALL)

    if not match:
        print(f"ERROR: Could not find Route {route_num} gpxData in RouteData.kt")
        return False

    gpx_json = match.group(2)
    gpx_data = json.loads(gpx_json)
    coords = gpx_data['coordinates']

    print(f"\nFound {len(coords)} coordinates in Route {route_num} gpxData")

    # Calculate cumulative distance for each coordinate
    cumulative_dist = [0.0]
    for i in range(1, len(coords)):
        lon1, lat1 = coords[i-1][0], coords[i-1][1]
        lon2, lat2 = coords[i][0], coords[i][1]
        dist = haversine_distance(lon1, lat1, lon2, lat2)
        cumulative_dist.append(cumulative_dist[-1] + dist)

    total_dist = cumulative_dist[-1]
    print(f"Total GPX distance: {total_dist:.2f}km")

    # Scale profile to match GPX distance
    profile_total = profile[-1][0]
    if abs(total_dist - profile_total) > 0.1:
        print(f"Scaling profile from {profile_total:.2f}km to {total_dist:.2f}km")
        scale = total_dist / profile_total
        profile = [(km * scale, elev) for km, elev in profile]

    # Update elevations
    updated_coords = []
    for i, coord in enumerate(coords):
        lon, lat = coord[0], coord[1]
        km = cumulative_dist[i]
        new_elev = round(interpolate_elevation(km, profile), 1)
        updated_coords.append([lon, lat, new_elev])

    # Show comparison
    print("\nElevation update preview:")
    print(f"  Start: old={coords[0][2] if len(coords[0]) > 2 else 'N/A'}, new={updated_coords[0][2]}")

    max_idx = max(range(len(updated_coords)), key=lambda i: updated_coords[i][2])
    print(f"  Peak ({cumulative_dist[max_idx]:.2f}km): new={updated_coords[max_idx][2]}m")

    print(f"  End: old={coords[-1][2] if len(coords[-1]) > 2 else 'N/A'}, new={updated_coords[-1][2]}")

    # Create updated gpxData
    updated_gpx = {"type": "LineString", "coordinates": updated_coords}
    new_gpx_json = json.dumps(updated_gpx, separators=(',', ':'))

    # Update content
    new_content = content[:match.start(2)] + new_gpx_json + content[match.end(2):]

    with open(routedata_path, 'w') as f:
        f.write(new_content)

    print(f"\nUpdated RouteData.kt for Route {route_num}")
    return True


def process_route(route_num: int, update: bool = False):
    """Process a single route."""
    print(f"\n{'='*60}")
    print(f"Processing Route {route_num}")
    print('='*60)

    # Download image
    image_path = download_profile_image(route_num)

    # Extract profile
    profile = extract_profile(image_path, route_num)

    # Sample to reduce points
    sampled = sample_profile(profile, 200)
    print(f"Sampled to {len(sampled)} points")

    # Print summary
    print_profile_summary(sampled, route_num)

    # Save to JSON
    save_profile(sampled, route_num)

    # Update RouteData.kt if requested
    if update:
        update_route_data(sampled, route_num)

    return sampled


def main():
    if len(sys.argv) < 2:
        print("Usage: python3 extract_all_routes.py <route_number|all> [--update]")
        print("Example: python3 extract_all_routes.py 1")
        print("         python3 extract_all_routes.py all --update")
        sys.exit(1)

    update = "--update" in sys.argv
    route_arg = sys.argv[1]

    if route_arg == "all":
        for route_num in range(1, 21):
            try:
                process_route(route_num, update)
            except Exception as e:
                print(f"ERROR processing route {route_num}: {e}")
    else:
        try:
            route_num = int(route_arg)
            if route_num < 1 or route_num > 20:
                print("Route number must be between 1 and 20")
                sys.exit(1)
            process_route(route_num, update)
        except ValueError:
            print(f"Invalid route number: {route_arg}")
            sys.exit(1)


if __name__ == "__main__":
    main()
