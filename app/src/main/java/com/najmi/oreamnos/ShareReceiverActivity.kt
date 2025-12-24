package com.najmi.oreamnos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.najmi.oreamnos.utils.PreferencesManager

/**
 * Activity that receives shared content and displays it in a bottom sheet.
 * This activity is transparent and only serves as a host for ShareBottomSheetFragment.
 */
class ShareReceiverActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefsManager = PreferencesManager(this)

        // Apply saved theme
        applyTheme(prefsManager.getTheme())

        // Get shared content
        val sharedContent = getSharedContent()

        if (sharedContent.isNullOrEmpty()) {
            finish()
            return
        }

        // Show bottom sheet
        val bottomSheet = ShareBottomSheetFragment.newInstance(sharedContent)
        bottomSheet.show(supportFragmentManager, "ShareBottomSheet")
    }

    /**
     * Called when the bottom sheet is dismissed.
     * This is triggered via the fragment's onDismiss callback.
     */
    fun onBottomSheetDismissed() {
        if (!isFinishing) {
            finish()
        }
    }

    /**
     * Gets the shared content from the intent.
     * Supports both ACTION_SEND (share sheet) and PROCESS_TEXT (text selection menu).
     */
    private fun getSharedContent(): String? {
        val action = intent.action
        val type = intent.type

        // Handle share sheet intent (ACTION_SEND)
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                return intent.getStringExtra(Intent.EXTRA_TEXT)
            }
        }

        // Handle text selection menu intent (PROCESS_TEXT)
        if (Intent.ACTION_PROCESS_TEXT == action) {
            val processText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            return processText?.toString()
        }

        return null
    }

    /**
     * Applies the selected theme.
     */
    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            PreferencesManager.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            PreferencesManager.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
