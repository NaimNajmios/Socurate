package com.najmi.oreamnos.cardgen.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.ui.DraggableCanvasElement
import com.najmi.oreamnos.ui.components.AutoSizeText

@Composable
fun HeadlineQuoteCanvas(
    data: CardData.HeadlineQuote,
    config: CardConfig,
    modifier: Modifier = Modifier,
    onOffsetChange: (String, Pair<Float, Float>) -> Unit = { _, _ -> }
) {
    CardBackground(
        config = config,
        modifier = modifier.aspectRatio(1f)
    ) {
        DraggableCanvasElement(
            elementId = "headline_quote",
            cardConfig = config,
            onOffsetChange = onOffsetChange,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "HEADLINE",
                    color = CardTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 3.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Canvas(modifier = Modifier.size(32.dp)) {
                        val w = size.width
                        val h = size.height
                        val stroke = Stroke(width = 4.dp.toPx())
                        
                        drawArc(
                            color = Color(0xFFFFD100),
                            startAngle = 180f, sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(0f, 0f),
                            size = Size(w * 0.4f, h * 0.5f),
                            style = stroke
                        )
                        drawArc(
                            color = Color(0xFFFFD100),
                            startAngle = 180f, sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(w * 0.5f, 0f),
                            size = Size(w * 0.4f, h * 0.5f),
                            style = stroke
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        if (data.quoteAuthor.isNotBlank()) {
                            Text(
                                text = data.quoteAuthor.uppercase(),
                                color = Color(0xFFFFD100),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        AutoSizeText(
                            text = data.headline.uppercase(),
                            color = CardTextPrimary,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                lineHeight = 36.sp
                            ),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                HorizontalDivider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (data.subtext.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.subtext,
                            color = CardTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun drawQuoteMark(scope: DrawScope, color: Color) {
    val strokeWidthPx = with(scope) { 4.dp.toPx() }
    val stroke = Stroke(width = strokeWidthPx)
    val w = scope.size.width
    val h = scope.size.height

    scope.drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(0f, h * 0.1f),
        size = Size(w * 0.35f, h * 0.45f),
        style = stroke
    )

    scope.drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.45f, h * 0.1f),
        size = Size(w * 0.35f, h * 0.45f),
        style = stroke
    )
}
