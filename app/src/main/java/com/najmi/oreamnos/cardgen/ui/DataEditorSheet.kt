package com.najmi.oreamnos.cardgen.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.najmi.oreamnos.ui.components.NeoButton

/**
 * Bottom sheet for inputting text to generate card data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEditorSheet(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isExtracting: Boolean,
    onExtractClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "ARTICLE TEXT",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = TextUnit(2f, TextUnitType.Sp)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Taller than before since it's in a sheet
                placeholder = {
                    Text(
                        text = "Paste a football article here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                maxLines = 10,
                shape = RoundedCornerShape(0.dp)
            )
            Spacer(Modifier.height(16.dp))
            NeoButton(
                text = "Extract Card Data",
                onClick = onExtractClick,
                isLoading = isExtracting,
                enabled = inputText.isNotBlank() && !isExtracting,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
