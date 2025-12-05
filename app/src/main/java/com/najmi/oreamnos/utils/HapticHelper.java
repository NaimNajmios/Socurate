package com.mycompany.oreamnos.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

/**
 * Utility class for haptic feedback throughout the app.
 * Provides tactile responses for various user interactions.
 */
public class HapticHelper {

    private final Vibrator vibrator;

    /**
     * Creates a new HapticHelper instance.
     */
    public HapticHelper(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context
                    .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    /**
     * Light tap when generation starts.
     */
    public void onGenerationStart() {
        if (vibrator == null || !vibrator.hasVibrator())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(10);
        }
    }

    /**
     * Success vibration when generation completes.
     * Double tick pattern for positive feedback.
     */
    public void onGenerationComplete() {
        if (vibrator == null || !vibrator.hasVibrator())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Double tick: 50ms vibrate, 100ms pause, 50ms vibrate
            long[] pattern = { 0, 50, 100, 50 };
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            long[] pattern = { 0, 50, 100, 50 };
            vibrator.vibrate(pattern, -1);
        }
    }

    /**
     * Light click for copy action.
     */
    public void onCopy() {
        if (vibrator == null || !vibrator.hasVibrator())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(5);
        }
    }

    /**
     * Error vibration for failures.
     */
    public void onError() {
        if (vibrator == null || !vibrator.hasVibrator())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Single longer vibration for error
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }
}
