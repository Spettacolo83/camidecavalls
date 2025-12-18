#!/usr/bin/env python3
"""
Convert route GPX to Xcode-compatible GPX with waypoints for iOS Simulator.

Xcode requires:
- <wpt> (waypoint) tags, NOT <rte>/<rtept> or <trk>/<trkpt>
- <time> elements for movement speed calculation
- Waypoints sorted by time in ascending order
"""

import os
import re
from datetime import datetime, timedelta

# Configuration
INPUT_DIR = "ios"
OUTPUT_DIR = "ios/simulator"
START_TIME = datetime(2025, 1, 1, 10, 0, 0)
INTERVAL_SECONDS = 3  # Time between each waypoint (simulates walking speed)

def convert_gpx_for_ios(input_file, output_file):
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract route name
    name_match = re.search(r'<name>(.+?)</name>', content)
    route_name = name_match.group(1) if name_match else "Test Route"
    # Clean up route name (remove " - Start" suffix if present)
    route_name = re.sub(r'\s*-\s*Start$', '', route_name)

    # Try to extract route points - support both rtept and wpt formats
    # Format 1: <rtept lat="..." lon="..."><ele>...</ele></rtept>
    pattern_rtept = r'<rtept lat="([^"]+)" lon="([^"]+)">\s*<ele>([^<]+)</ele>\s*</rtept>'
    points = re.findall(pattern_rtept, content)

    if not points:
        # Format 2: <wpt lat="..." lon="..."><ele>...</ele><time>...</time>...</wpt>
        # This format already has timestamps, but we'll re-normalize them
        pattern_wpt = r'<wpt lat="([^"]+)" lon="([^"]+)">\s*<ele>([^<]+)</ele>'
        points = re.findall(pattern_wpt, content)

    if not points:
        print(f"  No route points found in {input_file}, skipping...")
        return False

    # Build Xcode-compatible GPX with waypoints
    output = '''<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="CamiDeCavalls-iOSSimulator"
     xmlns="http://www.topografix.com/GPX/1/1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
'''

    current_time = START_TIME
    for i, (lat, lon, ele) in enumerate(points):
        timestamp = current_time.strftime("%Y-%m-%dT%H:%M:%SZ")
        output += f'''  <wpt lat="{lat}" lon="{lon}">
    <ele>{ele}</ele>
    <time>{timestamp}</time>
    <name>{route_name} - Point {i + 1}</name>
  </wpt>
'''
        current_time += timedelta(seconds=INTERVAL_SECONDS)

    output += '</gpx>\n'

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(output)

    total_duration = (len(points) - 1) * INTERVAL_SECONDS
    minutes = total_duration // 60
    seconds = total_duration % 60
    print(f"  {len(points)} waypoints, duration: {minutes}m {seconds}s")
    return True

def convert_single(route_number):
    """Convert a single route file."""
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    input_file = os.path.join(INPUT_DIR, f"route_{route_number}.gpx")
    output_file = os.path.join(OUTPUT_DIR, f"route_{route_number}_simulator.gpx")

    if not os.path.exists(input_file):
        print(f"Error: {input_file} not found")
        return False

    print(f"Converting route_{route_number}.gpx for iOS Simulator...")
    return convert_gpx_for_ios(input_file, output_file)

def convert_all():
    """Convert all route files."""
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    converted = 0
    for filename in sorted(os.listdir(INPUT_DIR)):
        if filename.startswith("route_") and filename.endswith(".gpx") and "_simulator" not in filename:
            input_path = os.path.join(INPUT_DIR, filename)
            output_filename = filename.replace(".gpx", "_simulator.gpx")
            output_path = os.path.join(OUTPUT_DIR, output_filename)

            print(f"Converting {filename}...")
            if convert_gpx_for_ios(input_path, output_path):
                converted += 1

    print(f"\nConverted {converted} files to {OUTPUT_DIR}/")

if __name__ == "__main__":
    import sys
    if len(sys.argv) > 1:
        # Convert single route if number provided
        convert_single(sys.argv[1])
    else:
        # Convert all routes
        convert_all()
