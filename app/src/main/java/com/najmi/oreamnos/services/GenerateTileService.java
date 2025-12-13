package com.najmi.oreamnos.services;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.najmi.oreamnos.R;
import com.najmi.oreamnos.utils.PreferencesManager;

/**
 * Quick Settings Tile for fast content generation from clipboard.
 * User can add this tile to their Quick Settings panel.
 * Tapping it reads clipboard content and starts background generation.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class GenerateTileService extends TileService {

    private static final String TAG = "GenerateTileService";

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        super.onClick();

        // Get clipboard content
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String clipboardText = getClipboardText(clipboard);

        if (clipboardText == null || clipboardText.isEmpty()) {
            showToast(getString(R.string.tile_no_clipboard));
            return;
        }

        // Check if it looks like a URL
        if (!isUrl(clipboardText)) {
            showToast(getString(R.string.tile_no_clipboard));
            return;
        }

        // Update tile to show generating state
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setLabel(getString(R.string.tile_generating));
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }

        // Start generation service
        startGeneration(clipboardText);

        // Show toast feedback
        showToast(getString(R.string.tile_generating));

        // Reset tile state after a delay
        new android.os.Handler(getMainLooper()).postDelayed(this::updateTileState, 2000);
    }

    /**
     * Starts the content generation service with clipboard URL.
     */
    private void startGeneration(String url) {
        PreferencesManager prefs = new PreferencesManager(this);

        Intent serviceIntent = new Intent(this, ContentGenerationService.class);
        serviceIntent.setAction(ContentGenerationService.ACTION_GENERATE);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INPUT_TEXT, url);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INCLUDE_SOURCE, prefs.isSourceEnabled());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Log the action
        prefs.logInfo("Tile", "Generation started from Quick Settings tile");
    }

    /**
     * Updates tile state to reflect availability.
     */
    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile == null)
            return;

        tile.setLabel(getString(R.string.tile_label));
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_tile));
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    /**
     * Gets text from clipboard.
     */
    private String getClipboardText(ClipboardManager clipboard) {
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            return null;
        }

        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return null;
        }

        CharSequence text = clip.getItemAt(0).getText();
        return text != null ? text.toString().trim() : null;
    }

    /**
     * Simple URL check.
     */
    private boolean isUrl(String text) {
        if (text == null)
            return false;
        String lower = text.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /**
     * Shows a toast message.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
