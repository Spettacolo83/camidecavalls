#!/usr/bin/env python3
"""
Fix POI coordinates by converting UTM coordinates from official map to WGS84.
Updates the POI JSON with correct coordinates.
"""

import json
import math

def utm_to_wgs84(easting, northing, zone=31):
    """
    Convert UTM Zone 31N coordinates to WGS84 (latitude, longitude).

    This uses the standard UTM to WGS84 conversion formulas.
    Zone 31N covers Menorca (Balearic Islands).
    """
    # WGS84 parameters
    a = 6378137.0  # semi-major axis
    e = 0.081819191  # eccentricity
    e_sq = e * e

    # UTM parameters for Zone 31N
    k0 = 0.9996  # scale factor
    lon_origin = math.radians((zone - 1) * 6 - 180 + 3)  # Central meridian for zone 31 = 3°E

    # Remove false easting/northing
    x = easting - 500000.0
    y = northing

    # Calculate footprint latitude
    M = y / k0
    mu = M / (a * (1 - e_sq/4 - 3*e_sq*e_sq/64 - 5*e_sq*e_sq*e_sq/256))

    e1 = (1 - math.sqrt(1 - e_sq)) / (1 + math.sqrt(1 - e_sq))

    phi1 = mu + (3*e1/2 - 27*e1*e1*e1/32) * math.sin(2*mu) + \
           (21*e1*e1/16 - 55*e1*e1*e1*e1/32) * math.sin(4*mu) + \
           (151*e1*e1*e1/96) * math.sin(6*mu)

    # Calculate latitude and longitude
    C1 = e_sq * math.cos(phi1) * math.cos(phi1) / (1 - e_sq)
    T1 = math.tan(phi1) * math.tan(phi1)
    N1 = a / math.sqrt(1 - e_sq * math.sin(phi1) * math.sin(phi1))
    R1 = a * (1 - e_sq) / math.pow(1 - e_sq * math.sin(phi1) * math.sin(phi1), 1.5)
    D = x / (N1 * k0)

    latitude = phi1 - (N1 * math.tan(phi1) / R1) * \
               (D*D/2 - (5 + 3*T1 + 10*C1 - 4*C1*C1 - 9*e_sq) * D*D*D*D/24 + \
                (61 + 90*T1 + 298*C1 + 45*T1*T1 - 252*e_sq - 3*C1*C1) * D*D*D*D*D*D/720)

    longitude = lon_origin + \
                (D - (1 + 2*T1 + C1) * D*D*D/6 + \
                 (5 - 2*C1 + 28*T1 - 3*C1*C1 + 8*e_sq + 24*T1*T1) * D*D*D*D*D/120) / math.cos(phi1)

    # Convert to degrees
    latitude = math.degrees(latitude)
    longitude = math.degrees(longitude)

    return latitude, longitude


def fix_coordinates():
    """
    Update POI coordinates from official map data.
    """
    print("🗺️  Fixing POI Coordinates from Official Map")
    print("=" * 50)

    # Load UTM coordinates from map
    print("\n📖 Loading UTM coordinates from map...")
    with open('scripts/camidecavalls_pois/coordinates_from_map.json', 'r') as f:
        utm_coords = json.load(f)
    print(f"   Found {len(utm_coords)} POIs in map data")

    # Load current POI data
    print("\n📖 Loading current POI data...")
    with open('scripts/camidecavalls_pois/pois_all_translations_complete.json', 'r') as f:
        pois = json.load(f)
    print(f"   Found {len(pois)} POIs in JSON")

    # Track changes
    updated_count = 0
    not_found_count = 0
    changes = []

    print("\n🔄 Converting UTM to WGS84 and updating coordinates...")

    for poi in pois:
        poi_id = poi['id']

        if poi_id not in utm_coords:
            not_found_count += 1
            print(f"  ⚠️  POI {poi_id} not found in map data")
            continue

        # Get UTM coordinates (remember: map stores them as lat/lon but they're actually Y/X)
        utm_y = utm_coords[poi_id]['latitude']  # Northing (Y)
        utm_x = utm_coords[poi_id]['longitude']  # Easting (X)

        # Convert to WGS84
        new_lat, new_lon = utm_to_wgs84(utm_x, utm_y)

        # Get old coordinates
        old_lat = poi['latitude']
        old_lon = poi['longitude']

        # Calculate difference
        lat_diff = abs(new_lat - old_lat)
        lon_diff = abs(new_lon - old_lon)

        # Update if there's a significant difference (> 0.0001 degrees ≈ 11 meters)
        if lat_diff > 0.0001 or lon_diff > 0.0001:
            changes.append({
                'id': poi_id,
                'name': poi['names']['ca'],
                'old': f"{old_lat:.6f}, {old_lon:.6f}",
                'new': f"{new_lat:.6f}, {new_lon:.6f}",
                'diff_m': max(lat_diff * 111000, lon_diff * 111000)  # rough meters
            })

            poi['latitude'] = new_lat
            poi['longitude'] = new_lon
            updated_count += 1

    # Save updated JSON
    print(f"\n💾 Saving updated POI data...")
    output_file = 'scripts/camidecavalls_pois/pois_all_translations_complete.json'
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(pois, f, ensure_ascii=False, indent=2)

    # Save changes report
    report_file = 'scripts/camidecavalls_pois/coordinate_changes_report.json'
    with open(report_file, 'w', encoding='utf-8') as f:
        json.dump(changes, f, ensure_ascii=False, indent=2)

    # Print summary
    print("\n" + "=" * 50)
    print("📊 SUMMARY:")
    print(f"   Total POIs in JSON: {len(pois)}")
    print(f"   POIs from map: {len(utm_coords)}")
    print(f"   ✅ Updated: {updated_count}")
    print(f"   ⏭️  Not found in map: {not_found_count}")
    print(f"   📝 Changes report: {report_file}")

    if changes:
        print(f"\n🔍 TOP 10 BIGGEST CHANGES:")
        for change in sorted(changes, key=lambda x: x['diff_m'], reverse=True)[:10]:
            print(f"   POI {change['id']}: {change['name']}")
            print(f"     Old: {change['old']}")
            print(f"     New: {change['new']}")
            print(f"     Difference: ~{change['diff_m']:.0f} meters")

    print("=" * 50)

    # Also copy to app resources
    app_file = 'composeApp/src/commonMain/composeResources/files/pois.json'
    print(f"\n📱 Copying to app resources: {app_file}")
    with open(app_file, 'w', encoding='utf-8') as f:
        json.dump(pois, f, ensure_ascii=False, indent=2)
    print("   ✅ App resources updated")


if __name__ == '__main__':
    try:
        fix_coordinates()
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
