# Scripts and Data

This directory contains utility scripts and scraped data for the Camí de Cavalls app.

## Contents

### Scripts

- **add_elevation_to_routes.py** - Script to add elevation data to route coordinates using Open-Elevation API
- **update_route_descriptions.py** - Utility script to update route descriptions from scraped data

### Data

- **camidecavalls_pois/** - POI (Points of Interest) data scraped from camidecavalls.com
  - Multilingual POI data (6 languages: CA, ES, EN, FR, DE, IT)
  - POI images
  - POI categories: BEACH, NATURAL, HISTORIC

## Elevation Data

Route coordinates now include elevation data fetched from the Open-Elevation API (SRTM dataset, 30m resolution).
All 20 routes have been enriched with elevation values in the format `[longitude, latitude, elevation]`.

## Future Enhancements

- Implement interactive elevation charts with map synchronization
