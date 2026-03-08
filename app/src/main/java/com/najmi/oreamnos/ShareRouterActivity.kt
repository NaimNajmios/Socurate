package com.najmi.oreamnos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * An invisible intermediate transparent Activity that intercepts incoming Android
 * Share events. Depending on which `<activity-alias>` the user selected from the
 * OS Share Sheet ("Generate AI Post" vs "Create Card Graphic"), this router packages
 * the shared text and bounces the user directly into the corresponding tab inside MainActivity.
 */
class ShareRouterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = getSharedContent()
        
        if (sharedText.isNullOrEmpty()) {
            finish()
            return
        }

        // Determine intended destination based on the Component Name clicked by the user
        // These strings match the AndroidManifest.xml <activity-alias> exact names.
        val componentName = intent.component?.className
        val isCardDestination = componentName == "com.najmi.oreamnos.ShareToCardAlias"

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("shared_text", sharedText)
            if (isCardDestination) {
                putExtra("ROUTE_TO_CARD", true)
            } else {
                putExtra("AUTO_GENERATE", true)
            }
        }
        
        startActivity(mainIntent)
        finish()
    }

    /**
     * Extracts text from OS ACTION_SEND (Share Sheet) or ACTION_PROCESS_TEXT (Floating Text Menu).
     */
    private fun getSharedContent(): String? {
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type == "text/plain") {
            return intent.getStringExtra(Intent.EXTRA_TEXT)
        }

        if (Intent.ACTION_PROCESS_TEXT == action) {
            return intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        }

        return null
    }
}
