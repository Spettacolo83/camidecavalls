#!/usr/bin/env python3
"""
Script to update RouteData.kt with multilingual route descriptions from JSON.
"""

import json
import re

# Paths
JSON_PATH = "/Users/stefanorussello/Documents/Projects/KotlinMultiplatform/CamíDeCavalls/scripts/camidecavalls_pois/routes_descriptions_complete.json"
ROUTE_DATA_PATH = "/Users/stefanorussello/Documents/Projects/KotlinMultiplatform/CamíDeCavalls/composeApp/src/commonMain/kotlin/com/followmemobile/camidecavalls/data/RouteData.kt"
DATABASE_USE_CASE_PATH = "/Users/stefanorussello/Documents/Projects/KotlinMultiplatform/CamíDeCavalls/composeApp/src/commonMain/kotlin/com/followmemobile/camidecavalls/domain/usecase/route/InitializeDatabaseUseCase.kt"

def escape_kotlin_string(text):
    """Escape special characters for Kotlin strings."""
    # Replace backslashes first
    text = text.replace('\\', '\\\\')
    # Replace quotes
    text = text.replace('"', '\\"')
    # Replace dollar signs (for template strings)
    text = text.replace('$', '\\$')
    # Replace newlines with \n
    text = text.replace('\n', '\\n')
    return text

def load_descriptions():
    """Load route descriptions from JSON file."""
    print(f"Loading descriptions from {JSON_PATH}...")
    with open(JSON_PATH, 'r', encoding='utf-8') as f:
        data = json.load(f)

    # Create a mapping from route number to descriptions
    descriptions_map = {}
    for route in data:
        route_num = route['number']
        descriptions_map[route_num] = route['descriptions']

    print(f"Loaded descriptions for {len(descriptions_map)} routes")
    return descriptions_map

def update_route_data(descriptions_map):
    """Update RouteData.kt with multilingual descriptions."""
    print(f"\nReading {ROUTE_DATA_PATH}...")
    with open(ROUTE_DATA_PATH, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find all Route objects and add description fields
    # Pattern to match Route objects with single-line string descriptions
    route_pattern = r'(Route\(\s*id = \d+,\s*number = (\d+),.*?estimatedDurationMinutes = \d+,\s*description = )"([^"]*)",(\s*gpxData = """)'

    def replace_route(match):
        route_prefix = match.group(1)  # Everything before description content
        route_number = int(match.group(2))  # Route number
        old_description = match.group(3)  # Old description content (escaped)
        gpx_prefix = match.group(4)  # After description, before gpxData

        if route_number not in descriptions_map:
            print(f"  Warning: No descriptions found for route {route_number}, keeping old format")
            return match.group(0)

        descriptions = descriptions_map[route_number]

        # Escape all descriptions for single-line Kotlin strings
        desc_ca = escape_kotlin_string(descriptions.get('ca', ''))
        desc_es = escape_kotlin_string(descriptions.get('es', ''))
        desc_en = escape_kotlin_string(descriptions.get('en', ''))
        desc_de = escape_kotlin_string(descriptions.get('de', ''))
        desc_fr = escape_kotlin_string(descriptions.get('fr', ''))
        desc_it = escape_kotlin_string(descriptions.get('it', ''))

        # Use Italian as the default fallback description
        default_desc = desc_it

        # Build new route with all description fields
        new_route = f'{route_prefix}"{default_desc}",\n'
        new_route += f'            descriptionCa = "{desc_ca}",\n'
        new_route += f'            descriptionEs = "{desc_es}",\n'
        new_route += f'            descriptionEn = "{desc_en}",\n'
        new_route += f'            descriptionDe = "{desc_de}",\n'
        new_route += f'            descriptionFr = "{desc_fr}",\n'
        new_route += f'            descriptionIt = "{desc_it}",\n'
        new_route += f'            {gpx_prefix}'

        print(f"  Updated route {route_number} with 6 language descriptions")
        return new_route

    # Replace all routes
    new_content = re.sub(route_pattern, replace_route, content, flags=re.DOTALL)

    # Check if any replacements were made
    if new_content == content:
        print("  WARNING: No routes were updated! Pattern might not be matching.")
        return False

    # Write updated content
    print(f"\nWriting updated content to {ROUTE_DATA_PATH}...")
    with open(ROUTE_DATA_PATH, 'w', encoding='utf-8') as f:
        f.write(new_content)

    print("RouteData.kt updated successfully!")
    return True

def update_database_version():
    """Increment DATABASE_VERSION from 9 to 10."""
    print(f"\nUpdating DATABASE_VERSION in {DATABASE_USE_CASE_PATH}...")
    with open(DATABASE_USE_CASE_PATH, 'r', encoding='utf-8') as f:
        content = f.read()

    # Update version from 9 to 10
    old_line = 'private const val DATABASE_VERSION = 9'
    new_line = 'private const val DATABASE_VERSION = 10'

    if old_line in content:
        new_content = content.replace(old_line, new_line)

        # Also add a comment about version 10
        version_9_comment = '// Version 9: Reordered Route 11 coordinates to fix 7.7km jump (start/end were swapped)'
        version_10_comment = '// Version 10: Added multilingual route descriptions (CA, ES, EN, DE, FR, IT)'
        new_content = new_content.replace(
            version_9_comment,
            version_9_comment + '\n        ' + version_10_comment
        )

        with open(DATABASE_USE_CASE_PATH, 'w', encoding='utf-8') as f:
            f.write(new_content)

        print("DATABASE_VERSION updated from 9 to 10")
        return True
    else:
        print(f"Warning: Could not find DATABASE_VERSION = 9 in file")
        return False

def main():
    print("=" * 60)
    print("Updating RouteData.kt with multilingual descriptions")
    print("=" * 60)

    # Load descriptions from JSON
    descriptions_map = load_descriptions()

    # Update RouteData.kt
    if not update_route_data(descriptions_map):
        print("\n" + "=" * 60)
        print("✗ Failed to update RouteData.kt")
        print("=" * 60)
        return

    # Update DATABASE_VERSION
    update_database_version()

    print("\n" + "=" * 60)
    print("✓ All updates completed successfully!")
    print("=" * 60)
    print("\nSummary:")
    print(f"  - Updated {len(descriptions_map)} routes with 6 language descriptions each")
    print(f"  - Total descriptions added: {len(descriptions_map) * 6} = 120 descriptions")
    print(f"  - DATABASE_VERSION incremented to 10")
    print("\nNext steps:")
    print("  1. Compile the project (Android + iOS)")
    print("  2. Test that localized descriptions appear correctly")
    print("  3. Verify database re-seeding happens on app launch")

if __name__ == '__main__':
    main()
