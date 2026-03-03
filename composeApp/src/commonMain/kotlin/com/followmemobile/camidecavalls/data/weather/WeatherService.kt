package com.followmemobile.camidecavalls.data.weather

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

@Serializable
data class OpenMeteoResponse(
    val daily: DailyData
)

@Serializable
data class DailyData(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double>
)

data class DayForecast(
    val date: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double
)

class WeatherService(private val httpClient: HttpClient) {

    private var cachedForecast: List<DayForecast>? = null
    private var cacheTimeMark: TimeSource.Monotonic.ValueTimeMark? = null

    private companion object {
        val CACHE_TTL = 5.minutes
        const val API_URL = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=39.95&longitude=3.86" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
            "&timezone=Europe/Madrid&forecast_days=6"
    }

    suspend fun getForecast(): List<DayForecast> {
        val mark = cacheTimeMark
        val cached = cachedForecast
        if (cached != null && mark != null && mark.elapsedNow() < CACHE_TTL) {
            return cached
        }

        return try {
            val response: OpenMeteoResponse = httpClient.get(API_URL).body()
            val days = response.daily.time.indices.map { i ->
                DayForecast(
                    date = response.daily.time[i],
                    weatherCode = response.daily.weatherCode[i],
                    tempMax = response.daily.temperatureMax[i],
                    tempMin = response.daily.temperatureMin[i]
                )
            }
            cachedForecast = days
            cacheTimeMark = TimeSource.Monotonic.markNow()
            days
        } catch (e: Exception) {
            // If we have stale cache, return it
            if (cached != null) return cached
            throw e
        }
    }

    fun weatherEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1 -> "🌤️"
        2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        56, 57 -> "🌧️"
        61, 63, 65 -> "🌧️"
        66, 67 -> "🌧️"
        71, 73, 75, 77 -> "🌨️"
        80, 81, 82 -> "🌧️"
        85, 86 -> "🌨️"
        95 -> "⛈️"
        96, 99 -> "⛈️"
        else -> "🌡️"
    }
}
