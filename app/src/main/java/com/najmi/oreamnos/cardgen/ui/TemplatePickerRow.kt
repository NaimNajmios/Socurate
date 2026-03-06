package com.najmi.oreamnos.cardgen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.cardgen.model.CardTemplate

/**
 * Horizontal scrollable row of card template selectors.
 * Displays all 4 templates; selected one is highlighted with primary color border.
 */
@Composable
fun TemplatePickerRow(
    selectedTemplate: CardTemplate,
    onTemplateSelected: (CardTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CardTemplate.all) { template ->
            TemplateChip(
                template = template,
                isSelected = template == selectedTemplate,
                onClick = { onTemplateSelected(template) }
            )
        }
    }
}

@Composable
private fun TemplateChip(
    template: CardTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                         else MaterialTheme.colorScheme.surface

    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        color = containerColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = template.displayName.uppercase(),
                color = textColor,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = template.description,
                color = textColor.copy(alpha = 0.65f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
        }
    }
}
