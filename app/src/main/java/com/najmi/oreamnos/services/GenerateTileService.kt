package com.najmi.oreamnos.services

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.najmi.oreamnos.R
import com.najmi.oreamnos.utils.PreferencesManager

/**
 * Quick Settings Tile for fast content generation from clipboard.
 * User can add this tile to their Quick Settings panel.
 * Tapping it reads clipboard content and starts background generation.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
class GenerateTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        // Get clipboard content
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipboardText = getClipboardText(clipboard)

        if (clipboardText.isNullOrEmpty()) {
            showToast(getString(R.string.tile_no_clipboard))
            return
        }

        // Check if it looks like a URL
        if (!isUrl(clipboardText)) {
            showToast(getString(R.string.tile_no_clipboard))
            return
        }

        // Update tile to show generating state
        qsTile?.apply {
            label = getString(R.string.tile_generating)
            state = Tile.STATE_ACTIVE
            updateTile()
        }

        // Start generation service
        startGeneration(clipboardText)

        // Show toast feedback
        showToast(getString(R.string.tile_generating))

        // Reset tile state after a delay
        Handler(mainLooper).postDelayed({ updateTileState() }, 2000)
    }

    /**
     * Starts the content generation service with clipboard URL.
     */
    private fun startGeneration(url: String) {
        val prefs = PreferencesManager(this)

        val serviceIntent = Intent(this, ContentGenerationService::class.java).apply {
            action = ContentGenerationService.ACTION_GENERATE
            putExtra(ContentGenerationService.EXTRA_INPUT_TEXT, url)
            putExtra(ContentGenerationService.EXTRA_INCLUDE_SOURCE, prefs.isSourceEnabled())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Log the action
        prefs.logInfo("Tile", "Generation started from Quick Settings tile")
    }

    /**
     * Updates tile state to reflect availability.
     */
    private fun updateTileState() {
        qsTile?.apply {
            label = getString(R.string.tile_label)
            icon = Icon.createWithResource(this@GenerateTileService, R.drawable.ic_tile)
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    /**
     * Gets text from clipboard.
     */
    private fun getClipboardText(clipboard: ClipboardManager?): String? {
        if (clipboard == null || !clipboard.hasPrimaryClip()) return null

        val clip = clipboard.primaryClip
        if (clip == null || clip.itemCount == 0) return null

        return clip.getItemAt(0)?.text?.toString()?.trim()
    }

    /**
     * Simple URL check.
     */
    private fun isUrl(text: String?): Boolean {
        if (text == null) return false
        val lower = text.lowercase()
        return lower.startsWith("http://") || lower.startsWith("https://")
    }

    /**
     * Shows a toast message.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "GenerateTileService"
    }
}
