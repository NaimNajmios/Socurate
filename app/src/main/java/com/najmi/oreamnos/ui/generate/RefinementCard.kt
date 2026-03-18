package com.najmi.oreamnos.ui.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.model.GenerationPill
import com.najmi.oreamnos.ui.components.FluidRefinementFlow
import com.najmi.oreamnos.ui.components.NeoButton

@Composable
fun RefinementCard(
    selectedOptions: List<String>,
    customPills: List<GenerationPill>,
    selectedPillIds: List<String>,
    onToggleOption: (String) -> Unit,
    onTogglePill: (String) -> Unit,
    onRegenerate: () -> Unit,
    onCreatePill: () -> Unit,
    onEditPill: (GenerationPill) -> Unit,
    modifier: Modifier = Modifier
) {
    val refinementLabels = listOf(
        "rephrase" to "Rephrase",
        "recheck_flow" to "Check Flow",
        "recheck_wording" to "Check Wording"
    )
    
    FluidRefinementFlow(
        options = refinementLabels,
        selectedOptions = selectedOptions,
        customPills = customPills,
        selectedPillIds = selectedPillIds,
        onToggleOption = onToggleOption,
        onTogglePill = onTogglePill,
        onCreatePill = onCreatePill,
        onEditPill = onEditPill,
        onRegenerate = onRegenerate,
        modifier = modifier
    )
}
