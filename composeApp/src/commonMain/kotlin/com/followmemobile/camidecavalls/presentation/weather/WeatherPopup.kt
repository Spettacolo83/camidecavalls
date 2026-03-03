package com.followmemobile.camidecavalls.presentation.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.followmemobile.camidecavalls.data.weather.DayForecast
import com.followmemobile.camidecavalls.data.weather.WeatherService
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings

sealed interface WeatherUiState {
    data object Hidden : WeatherUiState
    data object Loading : WeatherUiState
    data class Visible(val days: List<DayForecast>) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

private const val WEATHER_DETAIL_URL =
    "https://www.yr.no/en/forecast/daily-table/2-6453301/Spain/Balearic%20Islands/Balearic%20Islands/Menorca"

@Composable
fun WeatherPopup(
    state: WeatherUiState,
    strings: LocalizedStrings,
    weatherService: WeatherService,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is WeatherUiState.Hidden -> return
        is WeatherUiState.Loading -> WeatherLoadingIndicator(
            strings = strings,
            modifier = modifier
        )
        is WeatherUiState.Error -> WeatherErrorCard(
            message = state.message,
            onClose = onClose,
            modifier = modifier
        )
        is WeatherUiState.Visible -> WeatherCard(
            days = state.days,
            strings = strings,
            weatherService = weatherService,
            onClose = onClose,
            modifier = modifier
        )
    }
}

@Composable
private fun WeatherCard(
    days: List<DayForecast>,
    strings: LocalizedStrings,
    weatherService: WeatherService,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.weatherTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Forecast rows
            days.forEach { day ->
                val dayOfWeek = parseDayOfWeek(day.date)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.weatherDayName(dayOfWeek),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        text = weatherService.weatherEmoji(day.weatherCode),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${day.tempMin.toInt()}° / ${day.tempMax.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // More info button
            Button(
                onClick = { uriHandler.openUri(WEATHER_DETAIL_URL) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = strings.weatherMoreInfo,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp)
                )
            }
        }
    }
}

@Composable
private fun WeatherErrorCard(
    message: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🌐",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeatherLoadingIndicator(
    strings: LocalizedStrings,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF4FC3F7)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = strings.weatherTitle,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Parse ISO date string (yyyy-MM-dd) to day-of-week (1=Monday, 7=Sunday).
 * Uses a simple calculation without java.time for KMP compatibility.
 */
private fun parseDayOfWeek(dateStr: String): Int {
    val parts = dateStr.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val day = parts[2].toInt()

    // Tomohiko Sakamoto's algorithm (returns 0=Sunday, 1=Monday, ..., 6=Saturday)
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val y = if (month < 3) year - 1 else year
    val dow = (y + y / 4 - y / 100 + y / 400 + t[month - 1] + day) % 7
    // Convert to 1=Monday, 7=Sunday
    return if (dow == 0) 7 else dow
}
