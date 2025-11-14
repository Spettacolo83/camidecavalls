package com.followmemobile.camidecavalls.presentation.pois

import androidx.compose.ui.graphics.Color
import com.followmemobile.camidecavalls.domain.model.POIType

/**
 * Shared color palette for POI types so UI chips and map markers stay in sync.
 */
object PoiTypeColors {
    fun markerHex(type: POIType): String = when (type) {
        POIType.BEACH -> "#6FBAFF"      // Pastel blue
        POIType.NATURAL -> "#7FD17F"    // Pastel green
        POIType.HISTORIC -> "#FF8080"   // Pastel red/coral
    }

    fun chipTint(type: POIType): Color = when (type) {
        POIType.BEACH -> Color(0xFF6FBAFF)
        POIType.NATURAL -> Color(0xFF7FD17F)
        POIType.HISTORIC -> Color(0xFFFF8080)
    }
}
