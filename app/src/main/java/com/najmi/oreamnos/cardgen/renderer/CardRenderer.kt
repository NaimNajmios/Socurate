package com.najmi.oreamnos.cardgen.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.najmi.oreamnos.cardgen.model.CardConfig
import com.najmi.oreamnos.cardgen.model.CardData
import com.najmi.oreamnos.cardgen.model.ExportSize
import com.najmi.oreamnos.cardgen.ui.CardCanvas
import com.najmi.oreamnos.ui.theme.SocurateTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Renders a [CardData] + [CardConfig] combination to an off-screen [Bitmap]
 * at the pixel dimensions specified by [ExportSize].
 *
 * Approach:
 * - Inflates a [ComposeView] in a temporary [FrameLayout]
 * - Wraps the layout in the app's [SocurateTheme]
 * - Measures + lays out at export dimensions
 * - Calls [View.draw] into a software [Canvas]
 *
 * Note: must be called on the **main thread** since it interacts with Views.
 * Wrap in [withContext](Dispatchers.Main) when calling from coroutines.
 */
object CardRenderer {

    private const val TAG = "CardRenderer"

    /**
     * Renders [cardData] with [cardConfig] to a [Bitmap] at [exportSize] dimensions.
     *
     * This function must be called from the **main thread** — it creates and manipulates
     * Android Views. It suspends until the Compose layout pass is complete.
     */
    suspend fun renderToBitmap(
        context: Context,
        cardData: CardData,
        cardConfig: CardConfig,
        exportSize: ExportSize
    ): Bitmap = withContext(Dispatchers.Main) {
        val widthPx = exportSize.widthPx
        val heightPx = exportSize.heightPx

        Log.d(TAG, "Rendering card at ${widthPx}x${heightPx}px for ${cardData::class.simpleName}")

        suspendCancellableCoroutine { continuation ->
            val composeView = ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    SocurateTheme {
                        CardCanvas(
                            cardData = cardData,
                            cardConfig = cardConfig
                        )
                    }
                }
            }

            // Wrap in a FrameLayout so we can measure/layout safely
            val container = FrameLayout(context).apply {
                addView(
                    composeView,
                    FrameLayout.LayoutParams(widthPx, heightPx)
                )
            }

            // Force a measure + layout pass at our target dimensions
            val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
            container.measure(widthSpec, heightSpec)
            container.layout(0, 0, widthPx, heightPx)

            // Draw to a software bitmap
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            container.draw(canvas)

            Log.d(TAG, "Render complete — bitmap size: ${bitmap.width}x${bitmap.height}")
            continuation.resume(bitmap)
        }
    }
}
