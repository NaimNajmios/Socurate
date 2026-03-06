package com.najmi.oreamnos.cardgen.ui

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.najmi.oreamnos.cardgen.model.BackgroundType
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.PresetBackground
import com.najmi.oreamnos.cardgen.utils.ColorExtractor

/**
 * Bottom sheet for selecting the card background.
 * Three tabs: Gradient | Gallery | Preset.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundPickerSheet(
    currentConfig: CardConfig,
    onConfigUpdate: (CardConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(
        when (currentConfig.backgroundType) {
            BackgroundType.GALLERY -> 1
            BackgroundType.PRESET -> 2
            else -> 0
        }
    ) }
    val tabs = listOf("Gradient", "Gallery", "Preset")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "BACKGROUND",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when (selectedTab) {
                0 -> GradientTab(currentConfig = currentConfig, onConfigUpdate = onConfigUpdate)
                1 -> GalleryTab(currentConfig = currentConfig, onConfigUpdate = onConfigUpdate)
                2 -> PresetTab(currentConfig = currentConfig, onConfigUpdate = onConfigUpdate)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Tab 1: Gradient
// ──────────────────────────────────────────────────────────────

@Composable
private fun GradientTab(
    currentConfig: CardConfig,
    onConfigUpdate: (CardConfig) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Pick a club colour gradient",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 2-column grid of color swatches
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ColorExtractor.presetSwatches) { (name, start, end) ->
                val isSelected = currentConfig.colorPair == Pair(start, end)
                SwatchChip(
                    name = name,
                    start = start,
                    end = end,
                    isSelected = isSelected,
                    onClick = {
                        onConfigUpdate(
                            currentConfig.copy(
                                backgroundType = BackgroundType.GRADIENT,
                                colorPair = Pair(start, end),
                                backgroundBitmap = null
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SwatchChip(
    name: String,
    start: Color,
    end: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Brush.horizontalGradient(listOf(start, end))),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = name.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Tab 2: Gallery
// ──────────────────────────────────────────────────────────────

@Composable
private fun GalleryTab(
    currentConfig: CardConfig,
    onConfigUpdate: (CardConfig) -> Unit
) {
    val context = LocalContext.current
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }

    // Check permission
    LaunchedEffect(Unit) {
        hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true // Below API 33, READ_EXTERNAL_STORAGE is not needed for MediaStore
        }

        if (hasPermission) {
            // Query recent images from MediaStore
            val uris = mutableListOf<Uri>()
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, "$sortOrder LIMIT 30"
            )
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    uris.add(contentUri)
                }
            }
            imageUris = uris
        }
    }

    if (!hasPermission) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Gallery permission required.\nGrant permission and try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.height(320.dp)
    ) {
        items(imageUris) { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(0.dp))
                    .clickable {
                        // Load the bitmap in the ViewModel — for now pass URI
                        // ViewModel's setBackgroundBitmap will be called via callback
                        // We pass a decoded bitmap via a lambda
                        val bmp = try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                android.graphics.ImageDecoder.decodeBitmap(
                                    android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                android.provider.MediaStore.Images.Media.getBitmap(
                                    context.contentResolver, uri
                                )
                            }
                        } catch (e: Exception) { null }

                        if (bmp != null) {
                            onConfigUpdate(
                                currentConfig.copy(
                                    backgroundType = BackgroundType.GALLERY,
                                    backgroundBitmap = bmp,
                                    presetBackground = null
                                )
                            )
                        }
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Tab 3: Preset
// ──────────────────────────────────────────────────────────────

@Composable
private fun PresetTab(
    currentConfig: CardConfig,
    onConfigUpdate: (CardConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PresetBackground.entries.forEach { preset ->
            val isSelected = currentConfig.presetBackground == preset
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onConfigUpdate(
                            currentConfig.copy(
                                backgroundType = BackgroundType.PRESET,
                                presetBackground = preset,
                                backgroundBitmap = null
                            )
                        )
                    },
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color indicator box
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = preset.name.replace("_", " ").uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Preset background",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
