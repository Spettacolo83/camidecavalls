# Camí de Cavalls - POI Data

This folder contains the Points of Interest (POI) data for the Camí de Cavalls trail in Menorca, scraped from the official website [camidecavalls.com](https://www.camidecavalls.com).

## Files

- **`pois_all_translations_complete.json`**: Complete database of all 190 POIs with names and descriptions in 6 languages
- **`routes_descriptions_complete.json`**: Descriptions of the 20 trail stages in 6 languages
- **`translation_report.json`**: Detailed report of missing translations (generated after scraping)
- **`images/`**: Folder containing POI images downloaded from the website

## POI Data Structure

Each POI in the JSON file contains:

```json
{
  "id": "9792",
  "type": "HISTORIC",
  "latitude": 40.05783988870746,
  "longitude": 3.8825368671010954,
  "image_url": "https://www.camidecavalls.com/documents/documents/9191doc3.JPG",
  "names": {
    "ca": "Assentament costaner de Cala Morell",
    "es": "Asentamiento costero de Cala Morell",
    "en": "Coastal settlement of Cala Morell",
    "de": "Küstensiedlung Cala Morell",
    "fr": "Implantation côtière de Cala Morell",
    "it": "Insediamento costiero di Cala Morell"
  },
  "descriptions": {
    "ca": "Cala Morell acull un dels jaciments prehistòrics més espectaculars...",
    "es": "Cala Morell acoge uno de los yacimientos prehistóricos más espectaculares...",
    "en": "One of the most impressive prehistoric sites in Menorca...",
    "de": "In Cala Morell findet sich eine der bedeutendsten prähistorischen...",
    "fr": "C'est à Cala Morell qu'on trouve un des sites préhistoriques...",
    "it": "Cala Morell ospita uno dei giacimenti preistorici più spettacolari..."
  }
}
```

## How Scraping Works

### Language System Discovery

The official camidecavalls.com website uses a **session cookie system** to manage languages:

1. **Modern rewrite URLs** (not working for scraping):
   - Pattern: `https://www.camidecavalls.com/{lang}/poi/{id}`
   - Example: `https://www.camidecavalls.com/it/poi/9792`
   - Problem: Descriptions always remain in Catalan even when changing language in the URL

2. **Legacy URLs with session cookies** (working method):
   - First request: `portal.aspx?IDIOMA={num}` to set the language cookie
   - Second request: `Contingut.aspx?IdPub={id}` to get the translated content
   - IDIOMA values are: 1=Catalan, 2=Spanish, 3=English, 4=German, 5=French, 6=Italian

### Scraping Algorithm

The `scrape_poi_descriptions.py` script performs the following steps:

1. **Creates a cookie jar** to maintain the session
2. **For each language**:
   - Visits `portal.aspx?IDIOMA={num}` to set the cookie
   - Waits 200ms to ensure the cookie is saved
   - Visits `Contingut.aspx?IdPub={poi_id}` to get the content
   - Extracts title (H1) and description (P paragraphs, skipping generic intro)
3. **Handles fallbacks**: If a translation is missing, uses Catalan as fallback
4. **Generates report**: Tracks all missing translations in `translation_report.json`

### HTML Patterns

Content is extracted using regex:

```python
# Title (always present)
h1_match = re.search(r'<h1[^>]*>(.*?)</h1>', html, re.DOTALL)

# Paragraphs (skip first one which is generic)
p_matches = re.findall(r'<p[^>]*>(.*?)</p>', html, re.DOTALL)

# Filter generic paragraphs with keyword detection
generic_keywords = [
    'Camí de Cavalls és una de les millors maneres',  # CA
    'Camí de Cavalls es una de las mejores maneras',  # ES
    'Walking the Camí de Cavalls path is arguably',   # EN
    # ... etc
]
```

## Scraping Scripts

### Basic Usage

```bash
# Test with 3 POIs
python3 scrape_poi_descriptions.py

# Full scraping (190 POIs × 6 languages ≈ 13-15 minutes)
python3 scrape_poi_descriptions.py --full
```

### Features

- ✅ **Multilingual**: Downloads all 6 available languages
- ✅ **Smart fallback**: Uses Catalan if a translation is missing
- ✅ **Detailed report**: Generates `translation_report.json` with missing translations
- ✅ **Automatic backup**: Creates `.backup` of original file
- ✅ **Rate limiting**: 0.5s delay between requests to avoid server overload
- ✅ **Cookie management**: Maintains session for each language

### Script Output

```
📊 STATISTICS:
   Total POIs processed: 190
   ✅ Successfully updated: 190
   ❌ Failed: 0

⚠️  MISSING TRANSLATIONS REPORT:
   12 POIs have incomplete translations
   (Using Catalan as fallback for missing languages)

   Missing by language:
     DE: 8 POIs
     FR: 6 POIs
     IT: 4 POIs

   Detailed report saved to: scripts/camidecavalls_pois/translation_report.json
```

## POI Types

POIs are classified into 3 categories:

- **BEACH**: Beaches and coastal zones (e.g. Cala Mesquida, Port de Maó)
- **NATURAL**: Natural areas and biodiversity (e.g. S'Albufera des Grau, Far de Favàritx)
- **HISTORIC**: Cultural and historical heritage (e.g. Talaiots, Defense towers, Necropolises)

## Technical Notes

### Missing Translation Handling

Not all POIs have complete translations in all languages. The script:

1. **Attempts to download** all 6 languages
2. **Verifies presence** of content for each language
3. **Applies fallback**: If a language is missing, copies Catalan text
4. **Tracks fallbacks**: Saves in `translation_report.json` which POIs use fallback and for which languages

### Content Cleaning

The script removes:

- HTML tags (`<[^>]+>`)
- HTML entities (`&iacute;` → `í`, via `html.unescape()`)
- Multiple spaces (`\s+` → ` `)
- Generic introduction paragraph about Camí de Cavalls (present on all pages)

### Date Format

Dates present in the calendar are extracted but not saved, as they are relative to the page visit date.

## Maintenance

### When to Re-run Scraping

Re-run the script when:

- ✅ New POIs are added to the official website
- ✅ Descriptions are updated or corrected
- ✅ New languages are added to the website
- ✅ Images are modified

### How to Check if Update is Needed

1. Visit the [interactive map](https://www.camidecavalls.com/Mapa.aspx)
2. Check the number of displayed POIs
3. If > 190, there are new POIs to download

### Update Procedure

```bash
cd scripts
python3 scrape_poi_descriptions.py --full

# Check the report
cat camidecavalls_pois/translation_report.json

# If there are too many missing translations, check the website manually
# It could be a temporary server issue
```

## Coordinate Correction

### Problem Identified

During app testing, it emerged that some POIs had inaccurate coordinates, with discrepancies of several kilometers from the real position:

- **Cala Santandria** positioned near Cala Blanca
- **Cala Blanca** positioned at Ciutadella
- **Ciutadella** positioned far from the center

### Implemented Solution

**1. Script `scrape_poi_coordinates.py`**

Extracts UTM coordinates (EPSG:25831) from the official interactive map:

```python
# Extract POINT coordinates from map JavaScript
point_pattern = r'wktFormat\.readFeature\("POINT\(([0-9.]+)\s+([0-9.]+)\)"'
id_pattern = r'feature\.setId\(\'feature(\d+)\'\)'

# Save in coordinates_from_map.json
{
  "9666": {"latitude": 4430558.0, "longitude": 580953.0},  # UTM Y, X
  ...
}
```

**2. Script `fix_poi_coordinates.py`**

Converts UTM Zone 31N coordinates to WGS84 and updates the POI database:

```python
def utm_to_wgs84(easting, northing, zone=31):
    """
    Convert UTM Zone 31N (Menorca) coordinates to WGS84.
    Uses standard inverse projection formulas.
    """
    # WGS84 parameters
    a = 6378137.0  # semi-major axis
    e = 0.081819191  # eccentricity

    # UTM Zone 31N: central longitude 3°E
    lon_origin = math.radians(3)

    # Remove false easting/northing
    x = easting - 500000.0
    y = northing

    # Calculate latitude and longitude with inverse formulas
    # ... (complete formulas in script)

    return latitude, longitude
```

**3. Correction Results**

Generated file: `coordinate_changes_report.json`

```
🔧 Corrected POIs: 16 POIs
📏 Minimum threshold: > 800 meters difference

Top 5 corrections:
- POI 9666 (Ciutadella):     8.3 km moved
- POI 9665 (Cala Blanca):    3.7 km moved
- POI 9677 (Sant Tomàs):     4.0 km moved
- POI 9702 (El so de la natura): 7.1 km moved
- POI 9664 (Cala Santandria): 1.2 km moved
```

**4. App Integration**

- Incremented `POI_VERSION` from 2 to 3 in `InitializePOIsUseCase.kt`
- App automatically updates coordinates on next launch
- Versioning system based on `AppPreferences`

### Correction Scripts

```bash
# 1. Download coordinates from official map
python3 scripts/scrape_poi_coordinates.py

# 2. Convert UTM → WGS84 and update JSON
python3 scripts/fix_poi_coordinates.py

# Output:
# - scripts/camidecavalls_pois/pois_all_translations_complete.json (updated)
# - scripts/camidecavalls_pois/coordinate_changes_report.json (report)
# - composeApp/src/commonMain/composeResources/files/pois.json (copied)
```

## Change History

### 2025-11-09 - Multilingual Scraping Implementation

- ✅ Discovered session cookie system for translations
- ✅ Implemented scraper with cookie jar management
- ✅ Added fallback system for missing translations
- ✅ Created detailed translation report
- ✅ Downloaded all 190 POIs in 6 languages

### 2025-11-09 - Coordinate Correction from Official Map

- ✅ Identified inaccurate coordinate problem (POI swap)
- ✅ Implemented scraper for UTM coordinates from interactive map
- ✅ Created UTM Zone 31N → WGS84 converter
- ✅ Corrected 16 POIs with errors > 800 meters
- ✅ Integrated versioning system for automatic update
- ✅ Generated detailed correction report

### Resolved Issues

1. **Non-functional URL rewrite**: Modern URLs `/{lang}/poi/{id}` didn't respect language in URL
   - **Solution**: Use legacy system with session cookies

2. **Always Catalan translations**: Descriptions always appeared in Catalan
   - **Solution**: Discovered that visiting `portal.aspx?IDIOMA=X` is needed first to set cookie

3. **Generic paragraph**: All pages have a generic introductory paragraph
   - **Solution**: Keyword-based filter to identify and skip generic paragraphs

4. **Inaccurate coordinates**: Some POIs had wrong coordinates (up to 8.3 km error)
   - **Solution**: Scraping UTM coordinates from official map + WGS84 conversion

## Contacts and Source

- **Official website**: [camidecavalls.com](https://www.camidecavalls.com)
- **Repository**: Camí de Cavalls KMP App
- **Last modified**: 2025-11-09
- **Script**: `../scrape_poi_descriptions.py`
