package com.followmemobile.camidecavalls.util

/**
 * Debug flags for development.
 *
 * ⚠️ IMPORTANT: Set ALL flags to false and DEV_BASE_URL to "" before releasing to production!
 *
 * - FORCE_POI_SYNC: Forces POI sync on every app start, ignoring the 24h cache.
 *   Use when testing backend POI changes locally.
 *
 * - CLEAR_SYNC_CACHE: Clears all stored sync timestamps on app start,
 *   forcing a full re-download of all POIs (not just incremental).
 *   Use when the local DB is out of sync or corrupted.
 *
 * - DEV_BASE_URL: When non-empty, overrides the backend URL.
 *   Use your LAN IP for testing on real devices (e.g. "http://192.168.8.106:3002").
 *   Leave empty to use the platform default (10.0.2.2 for Android emulator, localhost for iOS sim).
 */
object DebugConfig {
    /** Force POI sync on every app start (bypasses 24h interval) */
    const val FORCE_POI_SYNC = true  // TODO: Set to false before release

    /** Clear sync cache on app start (forces full re-download) */
    const val CLEAR_SYNC_CACHE = true  // TODO: Set to false before release

    /** Override backend URL for LAN testing on real devices. Empty = use platform default. */
    const val DEV_BASE_URL = "http://192.168.8.106:3002"  // TODO: Set to "" before release
}
