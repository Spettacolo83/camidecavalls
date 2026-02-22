package com.followmemobile.camidecavalls.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings

enum class BottomTab {
    MAP, ROUTES, POI, NOTEBOOK, SETTINGS
}

private val DarkBackground = Color(0xFF1C1C2E)
private val SelectedBlue = Color(0xFF4FC3F7)
private val UnselectedGray = Color(0xFF8E8E9A)

@Composable
fun WeWardBottomBar(
    currentTab: BottomTab,
    strings: LocalizedStrings,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DarkBackground,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomBarItem(
                icon = Icons.Default.Map,
                label = strings.bottomBarMap,
                selected = currentTab == BottomTab.MAP,
                onClick = { onTabSelected(BottomTab.MAP) }
            )
            BottomBarItem(
                icon = Icons.Default.Explore,
                label = strings.bottomBarRoutes,
                selected = currentTab == BottomTab.ROUTES,
                onClick = { onTabSelected(BottomTab.ROUTES) }
            )
            BottomBarItem(
                icon = Icons.Default.Place,
                label = strings.bottomBarPoi,
                selected = currentTab == BottomTab.POI,
                onClick = { onTabSelected(BottomTab.POI) },
                weight = 0.8f
            )
            BottomBarItem(
                icon = Icons.Default.Book,
                label = strings.bottomBarNotebook,
                selected = currentTab == BottomTab.NOTEBOOK,
                onClick = { onTabSelected(BottomTab.NOTEBOOK) }
            )
            BottomBarItem(
                icon = Icons.Default.Settings,
                label = strings.bottomBarSettings,
                selected = currentTab == BottomTab.SETTINGS,
                onClick = { onTabSelected(BottomTab.SETTINGS) },
                weight = 1.3f
            )
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    weight: Float = 1f
) {
    val color = if (selected) SelectedBlue else UnselectedGray

    Column(
        modifier = Modifier
            .weight(weight)
            .selectable(
                selected = selected,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Text(
            text = label.uppercase(),
            color = color,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
