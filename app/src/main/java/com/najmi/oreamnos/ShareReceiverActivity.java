package com.najmi.oreamnos;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.najmi.oreamnos.utils.PreferencesManager;

/**
 * Activity that receives shared content and displays it in a bottom sheet.
 * This activity is transparent and only serves as a host for
 * ShareBottomSheetFragment.
 */
public class ShareReceiverActivity extends AppCompatActivity {

    private PreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsManager = new PreferencesManager(this);

        // Apply saved theme
        applyTheme(prefsManager.getTheme());

        // Get shared content
        String sharedContent = getSharedContent();

        if (sharedContent == null || sharedContent.isEmpty()) {
            finish();
            return;
        }

        // Show bottom sheet
        ShareBottomSheetFragment bottomSheet = ShareBottomSheetFragment.newInstance(sharedContent);
        bottomSheet.show(getSupportFragmentManager(), "ShareBottomSheet");
    }

    /**
     * Called when the bottom sheet is dismissed.
     * This is triggered via the fragment's onDismiss callback.
     */
    public void onBottomSheetDismissed() {
        if (!isFinishing()) {
            finish();
        }
    }

    /**
     * Gets the shared content from the intent.
     * Supports both ACTION_SEND (share sheet) and PROCESS_TEXT (text selection
     * menu).
     */
    private String getSharedContent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // Handle share sheet intent (ACTION_SEND)
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                return intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        // Handle text selection menu intent (PROCESS_TEXT)
        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            CharSequence processText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (processText != null) {
                return processText.toString();
            }
        }

        return null;
    }

    /**
     * Applies the selected theme.
     */
    private void applyTheme(String theme) {
        int mode;
        switch (theme) {
            case PreferencesManager.THEME_LIGHT:
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case PreferencesManager.THEME_DARK:
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case PreferencesManager.THEME_SYSTEM:
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
