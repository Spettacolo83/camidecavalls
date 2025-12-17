#!/usr/bin/env python3
"""
Update test-routes GPX files with elevation data from the extracted profiles.
"""

import os
import re
import json
import math

SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.dirname(SCRIPTS_DIR)
TEST_ROUTES_DIR = os.path.join(PROJECT_DIR, "test-routes")


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


def load_profile(route_num):
    """Load elevation profile for a route."""
    profile_path = os.path.join(SCRIPTS_DIR, f"route{route_num}_profile.json")
    if not os.path.exists(profile_path):
        print(f"  WARNING: Profile not found for route {route_num}")
        return None

    with open(profile_path, 'r') as f:
        data = json.load(f)
    return data['points']


def interpolate_elevation(km, profile):
    """Interpolate elevation at a given km position."""
    if not profile:
        return None

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


def update_gpx_file(gpx_path, route_num):
    """Update a GPX file with new elevation data."""
    profile = load_profile(route_num)
    if not profile:
        return False

    with open(gpx_path, 'r') as f:
        content = f.read()

    # Try Android format first: <rtept lat="..." lon="..."><ele>...</ele></rtept>
    pattern_rtept = r'<rtept lat="([^"]+)" lon="([^"]+)">\s*<ele>([^<]+)</ele>\s*</rtept>'
    matches = list(re.finditer(pattern_rtept, content))

    if matches:
        # Android format
        coords = [(float(m.group(2)), float(m.group(1))) for m in matches]
    else:
        # Try iOS format: <wpt lat="..." lon="...">
        pattern_wpt = r'<wpt lat="([^"]+)" lon="([^"]+)">'
        matches = list(re.finditer(pattern_wpt, content))
        if not matches:
            print(f"  WARNING: No route points found in {gpx_path}")
            return False
        coords = [(float(m.group(2)), float(m.group(1))) for m in matches]

    # Calculate cumulative distances
    cumulative_dist = [0.0]
    for i in range(1, len(coords)):
        lon1, lat1 = coords[i-1]
        lon2, lat2 = coords[i]
        dist = haversine_distance(lon1, lat1, lon2, lat2)
        cumulative_dist.append(cumulative_dist[-1] + dist)

    total_dist = cumulative_dist[-1]
    profile_dist = profile[-1][0]

    # Scale profile if needed
    if abs(total_dist - profile_dist) > 0.5:
        scale = total_dist / profile_dist
        scaled_profile = [(km * scale, elev) for km, elev in profile]
    else:
        scaled_profile = profile

    # Check if it's Android or iOS format
    is_android = '<rtept' in content

    if is_android:
        # Replace elevations in Android format
        new_content = content
        pattern_rtept_full = r'<rtept lat="([^"]+)" lon="([^"]+)">\s*<ele>[^<]+</ele>\s*</rtept>'
        for i, match in enumerate(re.finditer(pattern_rtept_full, content)):
            km = cumulative_dist[i]
            new_elev = interpolate_elevation(km, scaled_profile)
            if new_elev is not None:
                old_text = match.group(0)
                new_text = f'<rtept lat="{match.group(1)}" lon="{match.group(2)}">\n      <ele>{new_elev:.1f}</ele>\n    </rtept>'
                new_content = new_content.replace(old_text, new_text, 1)
    else:
        # iOS format - add elevation to <wpt> tags
        new_content = content
        # Match wpt with or without existing ele
        pattern_wpt_full = r'(<wpt lat="([^"]+)" lon="([^"]+)">)\s*(?:<ele>[^<]*</ele>\s*)?(<name>[^<]*</name>\s*)?(<time>[^<]*</time>\s*)(</wpt>)'

        def replace_wpt(match, idx_holder=[0]):
            idx = idx_holder[0]
            idx_holder[0] += 1
            km = cumulative_dist[idx] if idx < len(cumulative_dist) else cumulative_dist[-1]
            new_elev = interpolate_elevation(km, scaled_profile)
            elev_str = f"<ele>{new_elev:.1f}</ele>\n        " if new_elev else ""
            name_part = match.group(4) if match.group(4) else ""
            time_part = match.group(5) if match.group(5) else ""
            return f'{match.group(1)}\n        {elev_str}{name_part}{time_part}</wpt>'

        new_content = re.sub(pattern_wpt_full, replace_wpt, content)

    with open(gpx_path, 'w') as f:
        f.write(new_content)

    print(f"  Updated {os.path.basename(gpx_path)}: {len(coords)} points, {total_dist:.2f}km")
    return True


def main():
    print("Updating test-routes GPX files with new elevation profiles...")

    for platform in ['android', 'ios']:
        platform_dir = os.path.join(TEST_ROUTES_DIR, platform)
        if not os.path.exists(platform_dir):
            continue

        print(f"\n=== {platform.upper()} ===")

        # Get list of GPX files (only main files, not *_updated.gpx)
        gpx_files = [f for f in os.listdir(platform_dir)
                     if f.endswith('.gpx') and '_updated' not in f]

        for gpx_file in sorted(gpx_files):
            # Extract route number
            match = re.search(r'route_(\d+)\.gpx', gpx_file)
            if not match:
                continue

            route_num = int(match.group(1))
            gpx_path = os.path.join(platform_dir, gpx_file)

            print(f"Route {route_num}:")
            update_gpx_file(gpx_path, route_num)

        # Remove *_updated.gpx duplicates
        updated_files = [f for f in os.listdir(platform_dir) if '_updated.gpx' in f]
        for f in updated_files:
            path = os.path.join(platform_dir, f)
            os.remove(path)
            print(f"  Removed duplicate: {f}")

    print("\nDone!")


if __name__ == "__main__":
    main()
