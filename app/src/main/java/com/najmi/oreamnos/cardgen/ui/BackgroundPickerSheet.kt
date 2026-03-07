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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.najmi.oreamnos.cardgen.model.ImagePosition
import com.najmi.oreamnos.cardgen.model.PresetBackground
import com.najmi.oreamnos.cardgen.utils.ColorExtractor
import com.najmi.oreamnos.cardgen.utils.GradientBuilder

/**
 * Bottom sheet for selecting the card background.
 * Four tabs: Gradient | Gallery | Preset | Layout.
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
    // Add "Layout" tab - total 4 tabs now
    val tabs = listOf("Gradient", "Gallery", "Preset", "Layout")

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
                3 -> LayoutTab(currentConfig = currentConfig, onConfigUpdate = onConfigUpdate)
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

            // Use Bundle query to safely limit results (LIMIT in sortOrder crashes on API 30+)
            val queryArgs = android.os.Bundle().apply {
                putString(android.content.ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
                putInt(android.content.ContentResolver.QUERY_ARG_LIMIT, 30)
            }
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, queryArgs, null
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

// ──────────────────────────────────────────────────────────────
// Tab 4: Layout (Image Position)
// ──────────────────────────────────────────────────────────────

@Composable
private fun LayoutTab(
    currentConfig: CardConfig,
    onConfigUpdate: (CardConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Image Position
        Text(
            text = "IMAGE POSITION",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        // Grid of layout options (2 columns)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImagePosition.entries.take(2).forEach { position ->
                    LayoutOptionCard(
                        position = position,
                        isSelected = currentConfig.imagePosition == position,
                        onClick = {
                            onConfigUpdate(currentConfig.copy(imagePosition = position))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImagePosition.entries.drop(2).take(2).forEach { position ->
                    LayoutOptionCard(
                        position = position,
                        isSelected = currentConfig.imagePosition == position,
                        onClick = {
                            onConfigUpdate(currentConfig.copy(imagePosition = position))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImagePosition.entries.drop(4).forEach { position ->
                    LayoutOptionCard(
                        position = position,
                        isSelected = currentConfig.imagePosition == position,
                        onClick = {
                            onConfigUpdate(currentConfig.copy(imagePosition = position))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section: Image Opacity
        if (currentConfig.backgroundBitmap != null || currentConfig.imagePosition == ImagePosition.MINIMAL) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "IMAGE OPACITY: ${(currentConfig.imageOpacity * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Slider(
                value = currentConfig.imageOpacity,
                onValueChange = { onConfigUpdate(currentConfig.copy(imageOpacity = it)) },
                valueRange = 0.1f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Section: Scrim Overlay
        if (currentConfig.backgroundBitmap != null && 
            currentConfig.imagePosition != ImagePosition.SPLIT_LEFT &&
            currentConfig.imagePosition != ImagePosition.SPLIT_RIGHT) {
            
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SHOW OVERLAY",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Darkens image for text readability",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = currentConfig.showScrim,
                    onCheckedChange = { onConfigUpdate(currentConfig.copy(showScrim = it)) }
                )
            }

            // Scrim intensity slider
            if (currentConfig.showScrim) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "OVERLAY INTENSITY",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(GradientBuilder.ScrimType.entries.toList()) { scrimType ->
                        val isSelected = currentConfig.scrimType == scrimType
                        val label = when (scrimType) {
                            GradientBuilder.ScrimType.DARK -> "Dark"
                            GradientBuilder.ScrimType.LIGHT -> "Light"
                            GradientBuilder.ScrimType.MINIMAL -> "Min"
                            GradientBuilder.ScrimType.NONE -> "None"
                            GradientBuilder.ScrimType.HORIZONTAL -> "H"
                            GradientBuilder.ScrimType.REVERSE_HORIZONTAL -> "R"
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clickable {
                                    onConfigUpdate(currentConfig.copy(scrimType = scrimType))
                                },
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.outline
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                   else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Info text
        if (currentConfig.backgroundBitmap == null && currentConfig.imagePosition != ImagePosition.BACKGROUND) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "💡 Tip: Select an image from the Gallery tab to use image-based layouts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun LayoutOptionCard(
    position: ImagePosition,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.outline
        ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
               else MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Mini preview of layout
            when (position) {
                ImagePosition.BACKGROUND -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    ) {
                        // Text preview
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "Aa",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                ImagePosition.SPLIT_LEFT -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(0.55f)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.45f)
                                .height(48.dp)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Aa",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
                ImagePosition.SPLIT_RIGHT -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(0.45f)
                                .height(48.dp)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Aa",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.55f)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )
                    }
                }
                ImagePosition.OVERLAY_TOP -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(20.dp)
                                .background(Color.Black.copy(alpha = 0.6f))
                        )
                    }
                }
                ImagePosition.CUTOUT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        // Cutout preview - center circle
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Aa",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }
                ImagePosition.MINIMAL -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        )
                        Text(
                            text = "Aa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Label below the preview
    Text(
        text = position.displayName,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) MaterialTheme.colorScheme.primary 
               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = Modifier.padding(top = 4.dp)
    )
}
