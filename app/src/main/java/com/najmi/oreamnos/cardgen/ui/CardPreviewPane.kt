package com.najmi.oreamnos.cardgen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.CardTemplate
import com.najmi.oreamnos.cardgen.renderer.BreakingNewsCanvas
import com.najmi.oreamnos.cardgen.renderer.DetailedScoreboardCanvas
import com.najmi.oreamnos.cardgen.renderer.HeadlineQuoteCanvas
import com.najmi.oreamnos.cardgen.renderer.MatchPreviewCanvas
import com.najmi.oreamnos.cardgen.renderer.MatchStatsComparisonCanvas
import com.najmi.oreamnos.cardgen.renderer.OnThisDayCanvas
import com.najmi.oreamnos.cardgen.renderer.PlayerSpotlightCanvas
import com.najmi.oreamnos.cardgen.renderer.SocialPostCanvas
import com.najmi.oreamnos.cardgen.renderer.StartingXICanvas
import com.najmi.oreamnos.cardgen.renderer.TopStatsCanvas
import com.najmi.oreamnos.cardgen.renderer.TransferNewsCanvas
import com.najmi.oreamnos.cardgen.viewmodel.ExtractionState
import com.najmi.oreamnos.ui.components.EnhancedLoadingCard
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset

enum class EditableElement {
    TITLE,
    SUBTITLE,
    BODY,
    IMAGE,
    BADGE,
    STATS,
    QUOTE
}

data class ElementTapCallback(
    val onTitleTap: (() -> Unit)? = null,
    val onSubtitleTap: (() -> Unit)? = null,
    val onBodyTap: (() -> Unit)? = null,
    val onImageTap: (() -> Unit)? = null,
    val onBadgeTap: (() -> Unit)? = null,
    val onStatsTap: (() -> Unit)? = null,
    val onQuoteTap: (() -> Unit)? = null
)

/**
 * Live preview pane for the card generator.
 *
 * - Shows [EnhancedLoadingCard] while AI extraction is in progress.
 * - Renders the appropriate [CardCanvas] composable once data is ready.
 * - Scales to 90% screen width via [Modifier.fillMaxWidth] + [Modifier.scale].
 * - Supports tappable elements via [elementTapCallbacks] for quick editing.
 */
@Composable
fun CardPreviewPane(
    extractionState: ExtractionState,
    selectedTemplate: CardTemplate,
    cardConfig: CardConfig,
    onConfigUpdate: (CardConfig) -> Unit,
    elementTapCallbacks: ElementTapCallback = ElementTapCallback(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        when (extractionState) {
            is ExtractionState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(aspectForTemplate(selectedTemplate))) {
                    EnhancedLoadingCard()
                }
            }
            is ExtractionState.Success -> {
                CardCanvas(
                    cardData = extractionState.cardData,
                    cardConfig = cardConfig,
                    elementTapCallbacks = elementTapCallbacks,
                    onOffsetChange = { id, offset ->
                        val newOffsets = cardConfig.elementOffsets.toMutableMap()
                        newOffsets[id] = offset
                        onConfigUpdate(cardConfig.copy(elementOffsets = newOffsets))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is ExtractionState.Idle, is ExtractionState.Error -> {
                // Show empty placeholder matching card aspect ratio
                CardPlaceholder(template = selectedTemplate)
            }
        }
    }
}

/**
 * Routes to the correct canvas composable based on [cardData] type.
 */
@Composable
fun CardCanvas(
    cardData: CardData,
    cardConfig: CardConfig,
    onOffsetChange: (String, Pair<Float, Float>) -> Unit = { _, _ -> },
    elementTapCallbacks: ElementTapCallback = ElementTapCallback(),
    modifier: Modifier = Modifier
) {
    when (cardData) {

        is CardData.HeadlineQuote -> HeadlineQuoteCanvas(data = cardData, config = cardConfig, onOffsetChange = onOffsetChange, modifier = modifier)
        is CardData.PlayerSpotlight -> PlayerSpotlightCanvas(data = cardData, config = cardConfig, onOffsetChange = onOffsetChange, modifier = modifier)
        is CardData.TopStats -> TopStatsCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.TransferNews -> TransferNewsCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.BreakingNews -> BreakingNewsCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.MatchPreview -> MatchPreviewCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.DetailedScoreboard -> DetailedScoreboardCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.OnThisDay -> OnThisDayCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.StartingXI -> StartingXICanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.MatchStatsComparison -> MatchStatsComparisonCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.SocialPost -> SocialPostCanvas(data = cardData, config = cardConfig, modifier = modifier)
    }
}

@Composable
private fun CardPlaceholder(template: CardTemplate) {
    val aspectRatio = aspectForTemplate(template)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
    ) {
        // Empty placeholder styled to show the card boundary
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(aspectRatio),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        ) {}
    }
}

/** Aspect ratio for the card preview — all templates use compact square format. */
private fun aspectForTemplate(template: CardTemplate): Float = 1f

@Composable
fun DraggableCanvasElement(
    elementId: String,
    cardConfig: CardConfig,
    onOffsetChange: (String, Pair<Float, Float>) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val currentOffset = cardConfig.elementOffsets[elementId] ?: Pair(0f, 0f)
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    Box(
        modifier = modifier
            .offset(x = currentOffset.first.dp, y = currentOffset.second.dp)
            .pointerInput(elementId) {
                detectDragGestures { change: PointerInputChange, dragAmount: Offset ->
                    change.consume()
                    // Convert pixel dragAmount to DP
                    val deltaX = dragAmount.x / density
                    val deltaY = dragAmount.y / density
                    onOffsetChange(elementId, Pair(currentOffset.first + deltaX, currentOffset.second + deltaY))
                }
            }
    ) {
        content()
    }
}

@Composable
fun TappableCanvasElement(
    element: EditableElement,
    elementTapCallbacks: ElementTapCallback,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val onTap: () -> Unit = when (element) {
        EditableElement.TITLE -> elementTapCallbacks.onTitleTap ?: {}
        EditableElement.SUBTITLE -> elementTapCallbacks.onSubtitleTap ?: {}
        EditableElement.BODY -> elementTapCallbacks.onBodyTap ?: {}
        EditableElement.IMAGE -> elementTapCallbacks.onImageTap ?: {}
        EditableElement.BADGE -> elementTapCallbacks.onBadgeTap ?: {}
        EditableElement.STATS -> elementTapCallbacks.onStatsTap ?: {}
        EditableElement.QUOTE -> elementTapCallbacks.onQuoteTap ?: {}
    }
    
    Box(
        modifier = modifier
            .pointerInput(element) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            }
    ) {
        content()
    }
}
