# Scripts and Data

This directory contains utility scripts and scraped data for the Camí de Cavalls app.

## Scripts

### Elevation Profile Extraction

- **extract_all_routes.py** - Main script to extract elevation profiles from official camidecavalls.com images
  - Downloads profile images from `https://www.camidecavalls.com/Imas/General/perfil{N}d.png`
  - Extracts elevation data by detecting colored fill areas (green/orange)
  - Updates RouteData.kt with interpolated elevation values
  - Usage: `python3 extract_all_routes.py <route_number|all> [--update]`

- **update_test_gpx.py** - Updates test-routes GPX files with elevation data from extracted profiles
  - Supports both Android format (`<rtept>`) and iOS format (`<wpt>`)
  - Usage: `python3 update_test_gpx.py`

### Route Descriptions

- **update_route_descriptions.py** - Updates route descriptions from scraped multilingual data

### POI (Points of Interest)

- **scrape_poi_coordinates.py** - Scrapes POI coordinates from camidecavalls.com
- **scrape_poi_descriptions.py** - Scrapes POI descriptions in multiple languages
- **fix_poi_coordinates.py** - Fixes and validates POI coordinate data

## Data

- **camidecavalls_pois/** - POI data scraped from camidecavalls.com
  - Multilingual POI data (6 languages: CA, ES, EN, FR, DE, IT)
  - POI images
  - POI categories: BEACH, NATURAL, HISTORIC

## Elevation Data

Route elevation profiles are extracted from official camidecavalls.com graph images (`perfil{N}d.png`).
The colored difficulty images provide clear visual separation making pixel-based extraction reliable.

All 20 routes have elevation data in the format `[longitude, latitude, elevation]` stored in:
- `RouteData.kt` - Main app data source
- `test-routes/android/*.gpx` - Android test files
- `test-routes/ios/*.gpx` - iOS test files
