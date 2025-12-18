package com.followmemobile.camidecavalls.presentation.notebook

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import java.io.File

@Composable
actual fun ShareGpxButton(
    gpxContent: String,
    fileName: String,
    strings: LocalizedStrings
) {
    val context = LocalContext.current

    FilledTonalButton(
        onClick = {
            try {
                // Sanitize filename - replace invalid characters
                val safeFileName = fileName
                    .replace("/", "-")
                    .replace("\\", "-")
                    .replace(":", "-")

                // Write GPX content to cache directory
                val cacheDir = context.cacheDir
                val gpxFile = File(cacheDir, safeFileName)
                gpxFile.writeText(gpxContent)

                // Get content URI using FileProvider
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    gpxFile
                )

                // Create share intent with proper permissions
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/gpx+xml"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    // Set ClipData to ensure URI permissions are granted
                    clipData = ClipData.newRawUri("", contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Create chooser and add flag to it as well
                val chooserIntent = Intent.createChooser(shareIntent, strings.sessionShare).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(chooserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    ) {
        Icon(Icons.Default.Share, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(strings.sessionShare)
    }
}
