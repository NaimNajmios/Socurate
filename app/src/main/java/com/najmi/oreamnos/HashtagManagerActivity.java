package com.najmi.oreamnos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import com.najmi.oreamnos.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity for managing hashtags that are appended to generated posts.
 */
public class HashtagManagerActivity extends AppCompatActivity {

    private static final String TAG = "HashtagManagerActivity";

    private PreferencesManager prefsManager;
    private MaterialSwitch autoAppendSwitch;
    private TextInputEditText hashtagInput;
    private TextView charCounter;
    private ChipGroup tagCloudChipGroup;
    private TextView clearHashtags;
    private TextView saveAction;

    private List<String> currentTags = new ArrayList<>();
    private boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag_manager);

        prefsManager = new PreferencesManager(this);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        autoAppendSwitch = findViewById(R.id.autoAppendSwitch);
        hashtagInput = findViewById(R.id.hashtagInput);
        charCounter = findViewById(R.id.charCounter);
        tagCloudChipGroup = findViewById(R.id.tagCloudChipGroup);
        clearHashtags = findViewById(R.id.clearHashtags);
        saveAction = findViewById(R.id.saveAction);

        // Load current settings
        loadSettings();

        // Setup listeners
        setupListeners();

        isLoading = false;
    }

    private void loadSettings() {
        // Load auto-append enabled state
        boolean hashtagsEnabled = prefsManager.areHashtagsEnabled();
        autoAppendSwitch.setChecked(hashtagsEnabled);

        // Load hashtags
        String hashtagsStr = prefsManager.getHashtags();
        if (hashtagsStr != null && !hashtagsStr.isEmpty()) {
            hashtagInput.setText(hashtagsStr);
            parseAndDisplayTags(hashtagsStr);
        }
    }

    private void setupListeners() {
        // Auto-append toggle
        autoAppendSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isLoading) {
                prefsManager.setHashtagsEnabled(isChecked);
                showSavedFeedback();
            }
        });

        // Hashtag input text changes
        hashtagInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                updateCharCounter(text);
                if (!isLoading) {
                    parseAndDisplayTags(text);
                }
            }
        });

        // Clear hashtags button
        clearHashtags.setOnClickListener(v -> {
            hashtagInput.setText("");
            currentTags.clear();
            refreshTagCloud();
            if (!isLoading) {
                prefsManager.saveHashtags("");
                showSavedFeedback();
            }
        });

        // Save action
        saveAction.setOnClickListener(v -> {
            saveHashtags();
            Toast.makeText(this, "Hashtags saved", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Add tag chip click
        Chip addTagChip = findViewById(R.id.addTagChip);
        if (addTagChip != null) {
            addTagChip.setOnClickListener(v -> {
                // Focus on input and show keyboard
                hashtagInput.requestFocus();
                hashtagInput.setSelection(hashtagInput.getText() != null ? hashtagInput.getText().length() : 0);
            });
        }
    }

    private void updateCharCounter(String text) {
        // Count hashtags, not characters
        String[] parts = text.split("[\\s,]+");
        int tagCount = 0;
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                tagCount++;
            }
        }
        charCounter.setText(tagCount + " / 30");
    }

    private void parseAndDisplayTags(String text) {
        currentTags.clear();

        if (text == null || text.trim().isEmpty()) {
            refreshTagCloud();
            return;
        }

        // Split by spaces or commas
        String[] parts = text.split("[\\s,]+");
        for (String part : parts) {
            String tag = part.trim();
            if (!tag.isEmpty()) {
                // Ensure tag starts with #
                if (!tag.startsWith("#")) {
                    tag = "#" + tag;
                }
                if (!currentTags.contains(tag)) {
                    currentTags.add(tag);
                }
            }
        }

        refreshTagCloud();
    }

    private void refreshTagCloud() {
        // Remove all chips except the Add Tag chip
        tagCloudChipGroup.removeAllViews();

        // Add chips for each tag
        for (String tag : currentTags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setTextColor(getColor(R.color.md_theme_light_onSurface));
            chip.setChipBackgroundColorResource(R.color.md_theme_light_surface);
            chip.setChipStrokeColorResource(R.color.md_theme_light_outline);
            chip.setChipStrokeWidth(getResources().getDimension(R.dimen.chip_stroke_width));
            chip.setCloseIconVisible(true);
            chip.setCloseIconTintResource(R.color.md_theme_light_onSurfaceVariant);

            // Handle chip close (remove tag)
            chip.setOnCloseIconClickListener(v -> {
                currentTags.remove(tag);
                refreshTagCloud();
                updateInputFromTags();
            });

            tagCloudChipGroup.addView(chip);
        }

        // Add the "Add Tag" chip at the end
        Chip addChip = new Chip(this);
        addChip.setId(R.id.addTagChip);
        addChip.setText("Add Tag");
        addChip.setTextColor(getColor(R.color.md_theme_light_onSurfaceVariant));
        addChip.setChipBackgroundColor(null);
        addChip.setChipStrokeColorResource(R.color.md_theme_light_outline);
        addChip.setChipStrokeWidth(getResources().getDimension(R.dimen.chip_stroke_width));
        addChip.setChipIconResource(R.drawable.ic_add);
        addChip.setChipIconTintResource(R.color.md_theme_light_onSurfaceVariant);
        addChip.setOnClickListener(v -> {
            hashtagInput.requestFocus();
            hashtagInput.setSelection(hashtagInput.getText() != null ? hashtagInput.getText().length() : 0);
        });
        tagCloudChipGroup.addView(addChip);
    }

    private void updateInputFromTags() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentTags.size(); i++) {
            if (i > 0)
                sb.append(" ");
            sb.append(currentTags.get(i));
        }

        isLoading = true;
        hashtagInput.setText(sb.toString());
        isLoading = false;

        // Save after removing tag
        saveHashtags();
    }

    private void saveHashtags() {
        String hashtags = hashtagInput.getText() != null ? hashtagInput.getText().toString().trim() : "";
        prefsManager.saveHashtags(hashtags);
    }

    private void showSavedFeedback() {
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Auto-save when leaving
        saveHashtags();
    }
}
