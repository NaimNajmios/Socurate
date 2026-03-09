package com.najmi.oreamnos.cardgen.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import android.content.ContextWrapper
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
import kotlin.coroutines.resumeWithException

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

        // Extension to safely convert hardware bitmaps to software bitmaps (ARGB_8888)
        // because Canvas.draw() on a software canvas will crash if it encounters a HARDWARE bitmap.
        fun Bitmap?.toSoftware(): Bitmap? {
            if (this == null) return null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.config == Bitmap.Config.HARDWARE) {
                return this.copy(Bitmap.Config.ARGB_8888, false)
            }
            return this
        }

        // Pre-process the config to ensure no hardware bitmaps leak into the software rendering pass
        val safeConfig = cardConfig.copy(
            backgroundBitmap = cardConfig.backgroundBitmap.toSoftware(),
            cutoutBitmap = cardConfig.cutoutBitmap.toSoftware(),
            watermarkBitmap = cardConfig.watermarkBitmap.toSoftware()
        )

        suspendCancellableCoroutine { continuation ->
            val composeView = ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    SocurateTheme {
                        CardCanvas(
                            cardData = cardData,
                            cardConfig = safeConfig
                        )
                    }
                }
            }

            // Wrap in a FrameLayout and hide it
            val container = FrameLayout(context).apply {
                visibility = View.INVISIBLE
                addView(
                    composeView,
                    FrameLayout.LayoutParams(widthPx, heightPx)
                )
            }

            // Unwraps ContextWrapper to find the Activity
            tailrec fun Context.findActivity(): android.app.Activity? = when (this) {
                is android.app.Activity -> this
                is ContextWrapper -> baseContext.findActivity()
                else -> null
            }

            // Temporarily attach to the Activity to satisfy WindowRecomposer requirements
            val activity = context.findActivity()
            val rootView = activity?.findViewById<ViewGroup>(android.R.id.content)

            if (rootView == null) {
                continuation.resumeWithException(IllegalStateException("Cannot find root view to attach offline ComposeView"))
                return@suspendCancellableCoroutine
            }

            // Add with exact dimensions
            rootView.addView(container, ViewGroup.LayoutParams(widthPx, heightPx))

            // Wait for composition and layout pass to finish
            composeView.viewTreeObserver.addOnPreDrawListener(object : android.view.ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    composeView.viewTreeObserver.removeOnPreDrawListener(this)

                    try {
                        // Force final measure and layout to ensure it's exactly exportSize and not constrained by screen bounds
                        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
                        val heightSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
                        container.measure(widthSpec, heightSpec)
                        container.layout(0, 0, widthPx, heightPx)

                        // Draw to a software bitmap
                        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        container.draw(canvas)

                        Log.d(TAG, "Render complete — bitmap size: ${bitmap.width}x${bitmap.height}")
                        
                        rootView.removeView(container)
                        continuation.resume(bitmap)
                    } catch (e: Exception) {
                        rootView.removeView(container)
                        continuation.resumeWithException(e)
                    }
                    return true
                }
            })
            
            continuation.invokeOnCancellation {
                rootView.removeView(container)
            }
        }
    }
}
