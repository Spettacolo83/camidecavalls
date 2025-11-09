#!/usr/bin/env python3
"""
Scrape POI descriptions from camidecavalls.com website.
Each POI page contains multilingual descriptions that need to be extracted.

The website uses session cookies to manage language selection:
1. Visit portal.aspx?IDIOMA=X to set language cookie
2. Then visit Contingut.aspx?IdPub=Y to get translated content
"""

import json
import re
import time
from urllib.request import Request, urlopen, build_opener, HTTPCookieProcessor
from urllib.error import URLError
from http.cookiejar import CookieJar
from html import unescape

# Retry configuration
MAX_RETRIES = 5
INITIAL_RETRY_DELAY = 2  # seconds
MAX_RETRY_DELAY = 60  # seconds


def extract_poi_content(html):
    """
    Extract title and description from POI page HTML using regex.

    The page structure:
    - H1 tag contains the title
    - Multiple P tags, where:
      * First P is generic Camí de Cavalls intro (skip this)
      * Subsequent P tags contain the actual POI description
    """
    # Extract title from H1
    h1_match = re.search(r'<h1[^>]*>(.*?)</h1>', html, re.DOTALL | re.IGNORECASE)
    title = None
    if h1_match:
        title = re.sub('<[^>]+>', '', h1_match.group(1)).strip()
        title = unescape(title)  # Decode HTML entities

    # Extract all paragraphs
    p_matches = re.findall(r'<p[^>]*>(.*?)</p>', html, re.DOTALL | re.IGNORECASE)

    # Filter and clean paragraphs
    paragraphs = []
    generic_intro_keywords = [
        'Camí de Cavalls és una de les millors maneres',
        'Camí de Cavalls es una de las mejores maneras',
        'Walking the Camí de Cavalls path is arguably',
        'Der Camí de Cavalls',
        'Le Camí de Cavalls est',
        'Il Camí de Cavalls è forse'
    ]

    for p in p_matches:
        # Remove HTML tags
        clean_p = re.sub('<[^>]+>', '', p).strip()
        # Decode HTML entities
        clean_p = unescape(clean_p)
        # Remove extra whitespace
        clean_p = re.sub(r'\s+', ' ', clean_p)

        # Skip generic intro paragraph
        is_generic = any(keyword in clean_p for keyword in generic_intro_keywords)

        # Only keep substantial, non-generic paragraphs
        if len(clean_p) > 50 and not is_generic:
            paragraphs.append(clean_p)

    # Join paragraphs to form description
    description = '\n\n'.join(paragraphs) if paragraphs else None

    return {
        'title': title,
        'description': description
    }


def fetch_poi_page(poi_id, language='ca', opener=None, retry_count=0):
    """
    Fetch POI page for a specific language using session cookies.
    Includes automatic retry with exponential backoff for network errors.

    Languages: ca (Catalan), es (Spanish), en (English),
               de (German), fr (French), it (Italian)

    Args:
        poi_id: POI ID number
        language: Language code (ca, es, en, de, fr, it)
        opener: URLopener with cookie jar (will create one if None)
        retry_count: Current retry attempt (internal use)
    """
    # Map language codes to IDIOMA parameter values
    language_map = {
        'ca': 1,  # Català
        'es': 2,  # Castellano
        'en': 3,  # English
        'de': 4,  # Deutsche
        'fr': 5,  # Français
        'it': 6,  # Italiano
    }

    idioma = language_map.get(language, 1)

    # Create opener with cookie jar if not provided
    if opener is None:
        cookie_jar = CookieJar()
        opener = build_opener(HTTPCookieProcessor(cookie_jar))

    headers = {
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
    }

    try:
        # STEP 1: Set language cookie by visiting portal.aspx with IDIOMA parameter
        portal_url = f'https://www.camidecavalls.com/portal.aspx?IDIOMA={idioma}'
        req = Request(portal_url, headers=headers)
        opener.open(req, timeout=15)

        # Small delay to ensure cookie is set
        time.sleep(0.2)

        # STEP 2: Now fetch POI page with the language cookie set
        poi_url = f'https://www.camidecavalls.com/Contingut.aspx?IdPub={poi_id}'
        req = Request(poi_url, headers=headers)
        response = opener.open(req, timeout=15)
        html = response.read().decode('utf-8', errors='ignore')

        # Extract title and description from HTML
        result = extract_poi_content(html)

        # Basic validation
        if result['title'] and result['description']:
            return result
        else:
            print(f"  ⚠️  Warning: Incomplete data for {poi_id} ({language}): title={bool(result['title'])}, desc={bool(result['description'])}")
            return result

    except (URLError, OSError, Exception) as e:
        # Check if we should retry
        if retry_count < MAX_RETRIES:
            # Calculate delay with exponential backoff
            delay = min(INITIAL_RETRY_DELAY * (2 ** retry_count), MAX_RETRY_DELAY)

            # Add some jitter to avoid thundering herd
            import random
            delay = delay + random.uniform(0, 1)

            print(f"  ⚠️  Network error for {poi_id} ({language}), retry {retry_count + 1}/{MAX_RETRIES} in {delay:.1f}s...")
            time.sleep(delay)

            # Retry with a fresh cookie jar to avoid stale sessions
            fresh_jar = CookieJar()
            fresh_opener = build_opener(HTTPCookieProcessor(fresh_jar))

            return fetch_poi_page(poi_id, language, fresh_opener, retry_count + 1)
        else:
            print(f"  ❌ Failed after {MAX_RETRIES} retries: {poi_id} ({language}): {e}")
            return None


def scrape_poi_multilingual(poi_id):
    """
    Scrape POI content in all 6 languages.

    Uses session cookies to fetch each language version.
    """
    print(f"\n🔍 Scraping POI {poi_id}...")

    # Create a cookie jar to maintain session across requests
    cookie_jar = CookieJar()
    opener = build_opener(HTTPCookieProcessor(cookie_jar))

    languages = {
        'ca': 'Catalan',
        'es': 'Spanish',
        'en': 'English',
        'de': 'German',
        'fr': 'French',
        'it': 'Italian'
    }

    results = {}

    for lang_code, lang_name in languages.items():
        print(f"  📄 Fetching {lang_name}...", end=' ')
        result = fetch_poi_page(poi_id, lang_code, opener)

        if result and result.get('description'):
            results[lang_code] = result
            title_len = len(result.get('title') or '')
            desc_len = len(result.get('description') or '')
            print(f"✅ (title: {title_len} chars, desc: {desc_len} chars)")
        else:
            results[lang_code] = {'title': None, 'description': None}
            print("❌")

        # Be polite to the server
        time.sleep(0.5)

    return results


def update_poi_json(test_mode=True, test_poi_ids=None, force=False):
    """
    Update POI JSON file with scraped descriptions.

    Args:
        test_mode: If True, only process test_poi_ids
        test_poi_ids: List of POI IDs to test (e.g., ['9792', '9635', '9637'])
        force: If True, re-download even POIs that already have complete descriptions
    """
    json_path = 'scripts/camidecavalls_pois/pois_all_translations_complete.json'

    # Load existing POI data
    print(f"📖 Loading {json_path}...")
    with open(json_path, 'r', encoding='utf-8') as f:
        pois = json.load(f)

    print(f"   Found {len(pois)} POIs in total")

    # Filter POIs if in test mode
    if test_mode:
        if test_poi_ids:
            pois_to_process = [poi for poi in pois if poi['id'] in test_poi_ids]
            print(f"   🧪 TEST MODE: Processing {len(pois_to_process)} POIs: {test_poi_ids}")
        else:
            pois_to_process = pois[:3]  # Default: first 3 POIs
            print(f"   🧪 TEST MODE: Processing first 3 POIs")
    else:
        pois_to_process = pois
        print(f"   🚀 FULL MODE: Processing all {len(pois)} POIs")

    # Track statistics
    stats = {
        'total': len(pois_to_process),
        'success': 0,
        'failed': 0,
        'skipped': 0,
        'missing_translations': {}  # Track which POIs are missing which languages
    }

    # Process each POI
    for i, poi in enumerate(pois_to_process, 1):
        poi_id = poi['id']
        poi_name = poi['names']['ca']
        print(f"\n[{i}/{len(pois_to_process)}] POI {poi_id}: {poi_name}")

        # Check if POI already has complete descriptions (skip if already done and not force mode)
        if not force and 'descriptions' in poi and poi['descriptions']:
            # Check if all 6 languages have descriptions
            has_all_langs = all(
                lang in poi['descriptions'] and poi['descriptions'][lang]
                for lang in ['ca', 'es', 'en', 'de', 'fr', 'it']
            )
            if has_all_langs:
                print(f"  ⏭️  Already has complete descriptions, skipping...")
                stats['skipped'] += 1
                continue

        # Scrape multilingual content
        scraped_data = scrape_poi_multilingual(poi_id)

        # Check if we got valid data
        if scraped_data and scraped_data.get('ca', {}).get('description'):
            # Track missing translations for this POI
            missing_langs = []

            # Update the POI in the original array
            for poi_obj in pois:
                if poi_obj['id'] == poi_id:
                    # Catalan is the source, use it as fallback
                    ca_desc = scraped_data['ca']['description']
                    ca_title = scraped_data['ca']['title']

                    # Update descriptions with translations, using Catalan as fallback
                    poi_obj['descriptions'] = {}
                    for lang in ['ca', 'es', 'en', 'de', 'fr', 'it']:
                        if scraped_data.get(lang, {}).get('description'):
                            poi_obj['descriptions'][lang] = scraped_data[lang]['description']
                        else:
                            # Fallback to Catalan
                            poi_obj['descriptions'][lang] = ca_desc
                            missing_langs.append(lang)

                    # Update names with translations
                    for lang in ['ca', 'es', 'en', 'de', 'fr', 'it']:
                        if scraped_data.get(lang, {}).get('title'):
                            poi_obj['names'][lang] = scraped_data[lang]['title']
                        else:
                            poi_obj['names'][lang] = ca_title

                    # Track missing translations
                    if missing_langs:
                        stats['missing_translations'][poi_id] = {
                            'name': poi_obj['names']['ca'],
                            'missing': missing_langs
                        }
                        print(f"  ⚠️  Updated POI {poi_id} (using Catalan fallback for: {', '.join(missing_langs)})")
                    else:
                        print(f"  ✅ Updated POI {poi_id} with complete multilingual content")

                    stats['success'] += 1
                    break
        else:
            stats['failed'] += 1
            print(f"  ❌ Failed to get data for POI {poi_id}")

    # Save updated JSON (only in test mode to a separate file)
    if test_mode:
        output_path = 'scripts/camidecavalls_pois/pois_test_updated.json'
        print(f"\n💾 Saving test results to {output_path}...")
    else:
        # Backup original
        backup_path = 'scripts/camidecavalls_pois/pois_all_translations_complete.json.backup'
        import shutil
        shutil.copy(json_path, backup_path)
        print(f"\n💾 Backed up original to {backup_path}")
        output_path = json_path
        print(f"💾 Saving updated data to {json_path}...")

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(pois, f, ensure_ascii=False, indent=2)

    # Print statistics
    print(f"\n" + "="*50)
    print(f"📊 STATISTICS:")
    print(f"   Total POIs processed: {stats['total']}")
    print(f"   ✅ Successfully updated: {stats['success']}")
    print(f"   ❌ Failed: {stats['failed']}")
    print(f"   ⏭️  Skipped: {stats['skipped']}")

    # Report missing translations
    if stats['missing_translations']:
        print(f"\n⚠️  MISSING TRANSLATIONS REPORT:")
        print(f"   {len(stats['missing_translations'])} POIs have incomplete translations")
        print(f"   (Using Catalan as fallback for missing languages)")

        # Group by missing languages
        lang_summary = {'es': 0, 'en': 0, 'de': 0, 'fr': 0, 'it': 0}
        for poi_data in stats['missing_translations'].values():
            for lang in poi_data['missing']:
                if lang in lang_summary:
                    lang_summary[lang] += 1

        print(f"\n   Missing by language:")
        for lang, count in sorted(lang_summary.items(), key=lambda x: x[1], reverse=True):
            if count > 0:
                print(f"     {lang.upper()}: {count} POIs")

        # Save detailed report
        if not test_mode:
            report_path = 'scripts/camidecavalls_pois/translation_report.json'
            print(f"\n   Detailed report saved to: {report_path}")
            with open(report_path, 'w', encoding='utf-8') as f:
                json.dump(stats['missing_translations'], f, ensure_ascii=False, indent=2)

    print("="*50)


if __name__ == '__main__':
    import sys

    # Test with specific POIs first
    test_pois = ['9792', '9635', '9637']  # Cala Morell, Port de Maó, Cala Tortuga

    print("🏖️  Camí de Cavalls POI Description Scraper")
    print("="*50)

    # Check for --force flag
    force_mode = '--force' in sys.argv
    if force_mode:
        print("⚡ FORCE MODE: Will re-download even POIs with existing descriptions")

    if '--full' in sys.argv:
        print("⚠️  FULL MODE: This will update ALL POIs!")

        # Auto-accept if --yes flag is present, otherwise ask
        if '--yes' in sys.argv:
            print("Auto-confirmed with --yes flag")
            update_poi_json(test_mode=False, force=force_mode)
        else:
            response = input("Are you sure? (yes/no): ")
            if response.lower() == 'yes':
                update_poi_json(test_mode=False, force=force_mode)
            else:
                print("❌ Cancelled")
    else:
        print("🧪 Running in TEST mode (first few POIs only)")
        print("   Use --full flag to process all POIs")
        print("   Use --force flag to re-download POIs with existing descriptions")
        update_poi_json(test_mode=True, test_poi_ids=test_pois, force=force_mode)
