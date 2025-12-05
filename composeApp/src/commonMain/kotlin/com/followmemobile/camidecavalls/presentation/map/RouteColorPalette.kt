package com.followmemobile.camidecavalls.presentation.map

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Shared palette that generates up to 20 distinctive colors for the Camí de Cavalls routes.
 * Colors are created from the HSV color space so that the gradient is smooth between sections
 * and consistent across Map and Tracking screens.
 */
object RouteColorPalette {
    private const val ROUTE_COLOR_COUNT = 20
    private const val COLOR_SATURATION = 0.85f
    private const val COLOR_VALUE = 0.95f

    fun colorForIndex(index: Int): String {
        val hueStep = 360f / ROUTE_COLOR_COUNT
        val hue = (index % ROUTE_COLOR_COUNT) * hueStep
        return hsvToHex(hue, COLOR_SATURATION, COLOR_VALUE)
    }

    private fun hsvToHex(hue: Float, saturation: Float, value: Float): String {
        val normalizedHue = ((hue % 360f) + 360f) % 360f
        val chroma = value * saturation
        val huePrime = normalizedHue / 60f
        val secondLargestComponent = chroma * (1 - abs((huePrime % 2) - 1))

        val (red, green, blue) = when {
            huePrime < 1f -> Triple(chroma, secondLargestComponent, 0f)
            huePrime < 2f -> Triple(secondLargestComponent, chroma, 0f)
            huePrime < 3f -> Triple(0f, chroma, secondLargestComponent)
            huePrime < 4f -> Triple(0f, secondLargestComponent, chroma)
            huePrime < 5f -> Triple(secondLargestComponent, 0f, chroma)
            else -> Triple(chroma, 0f, secondLargestComponent)
        }

        val match = value - chroma
        val r = ((red + match) * 255).roundToInt().coerceIn(0, 255)
        val g = ((green + match) * 255).roundToInt().coerceIn(0, 255)
        val b = ((blue + match) * 255).roundToInt().coerceIn(0, 255)

        return "#${r.toString(16).padStart(2, '0').uppercase()}${g.toString(16).padStart(2, '0').uppercase()}${b.toString(16).padStart(2, '0').uppercase()}"
    }
}
