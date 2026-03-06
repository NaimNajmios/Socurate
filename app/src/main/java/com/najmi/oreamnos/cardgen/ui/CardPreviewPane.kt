package com.najmi.oreamnos.cardgen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.CardTemplate
import com.najmi.oreamnos.cardgen.model.ExportSize
import com.najmi.oreamnos.cardgen.renderer.HeadlineQuoteCanvas
import com.najmi.oreamnos.cardgen.renderer.MatchResultCanvas
import com.najmi.oreamnos.cardgen.renderer.PlayerSpotlightCanvas
import com.najmi.oreamnos.cardgen.renderer.TopStatsCanvas
import com.najmi.oreamnos.cardgen.renderer.NbaStyleCanvas
import com.najmi.oreamnos.cardgen.viewmodel.ExtractionState
import com.najmi.oreamnos.ui.components.EnhancedLoadingCard

/**
 * Live preview pane for the card generator.
 *
 * - Shows [EnhancedLoadingCard] while AI extraction is in progress.
 * - Renders the appropriate [CardCanvas] composable once data is ready.
 * - Scales to 90% screen width via [Modifier.fillMaxWidth] + [Modifier.scale].
 */
@Composable
fun CardPreviewPane(
    extractionState: ExtractionState,
    selectedTemplate: CardTemplate,
    cardConfig: CardConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        when (extractionState) {
            is ExtractionState.Loading -> {
                // Reuse existing loading card component — match aspect ratio to card
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(aspectForTemplate(selectedTemplate))) {
                    EnhancedLoadingCard()
                }
            }
            is ExtractionState.Success -> {
                CardCanvas(
                    cardData = extractionState.cardData,
                    cardConfig = cardConfig,
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
    modifier: Modifier = Modifier
) {
    when (cardData) {
        is CardData.MatchResult -> MatchResultCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.HeadlineQuote -> HeadlineQuoteCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.PlayerSpotlight -> PlayerSpotlightCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.TopStats -> TopStatsCanvas(data = cardData, config = cardConfig, modifier = modifier)
        is CardData.NbaStyleQuote -> NbaStyleCanvas(data = cardData, config = cardConfig, modifier = modifier)
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

/** Aspect ratio for the card preview based on the selected template's natural format. */
private fun aspectForTemplate(template: CardTemplate): Float = when (template) {
    CardTemplate.PlayerSpotlight, CardTemplate.NbaStyleQuote -> 4f / 5f  // Portrait
    else -> 1f                                 // Square
}
