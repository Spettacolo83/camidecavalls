#!/usr/bin/env python3
"""
Convert route GPX to track GPX with timestamps for Android Emulator simulation.

Android Emulator 30.0.26+ requires timestamps in GPX files for route playback.
This script converts <rte>/<rtept> format to <trk>/<trkpt> with proper timestamps.
"""

import os
import re
from datetime import datetime, timedelta

# Configuration
INPUT_DIR = "android"
OUTPUT_DIR = "android/emulator"
START_TIME = datetime(2025, 1, 1, 10, 0, 0)  # Start at 10:00:00
INTERVAL_SECONDS = 3  # Time between each point (simulates ~walking speed)

def convert_gpx(input_file, output_file):
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract route name
    name_match = re.search(r'<name>(.+?)</name>', content)
    route_name = name_match.group(1) if name_match else "Test Route"

    # Extract all route points with lat, lon, ele
    pattern = r'<rtept lat="([^"]+)" lon="([^"]+)">\s*<ele>([^<]+)</ele>\s*</rtept>'
    points = re.findall(pattern, content)

    if not points:
        print(f"  No route points found in {input_file}, skipping...")
        return False

    # Build new GPX with track format and timestamps
    output = '''<?xml version="1.0" encoding="utf-8"?>
<gpx xmlns="http://www.topografix.com/GPX/1/1"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     version="1.1"
     creator="CamiDeCavalls-EmulatorTest"
     xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
  <trk>
    <name>{name}</name>
    <trkseg>
'''.format(name=route_name)

    current_time = START_TIME
    for lat, lon, ele in points:
        timestamp = current_time.strftime("%Y-%m-%dT%H:%M:%SZ")
        output += f'''      <trkpt lat="{lat}" lon="{lon}">
        <ele>{ele}</ele>
        <time>{timestamp}</time>
      </trkpt>
'''
        current_time += timedelta(seconds=INTERVAL_SECONDS)

    output += '''    </trkseg>
  </trk>
</gpx>
'''

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(output)

    total_duration = (len(points) - 1) * INTERVAL_SECONDS
    minutes = total_duration // 60
    seconds = total_duration % 60
    print(f"  {len(points)} points, duration: {minutes}m {seconds}s")
    return True

def convert_all():
    # Create output directory if it doesn't exist
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Find all route GPX files
    converted = 0
    for filename in sorted(os.listdir(INPUT_DIR)):
        if filename.startswith("route_") and filename.endswith(".gpx") and "_emulator" not in filename:
            input_path = os.path.join(INPUT_DIR, filename)
            output_filename = filename.replace(".gpx", "_emulator.gpx")
            output_path = os.path.join(OUTPUT_DIR, output_filename)

            print(f"Converting {filename}...")
            if convert_gpx(input_path, output_path):
                converted += 1

    print(f"\nConverted {converted} files to {OUTPUT_DIR}/")

if __name__ == "__main__":
    convert_all()
