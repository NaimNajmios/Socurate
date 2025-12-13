package com.najmi.oreamnos;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.najmi.oreamnos.services.ContentGenerationService;
import com.najmi.oreamnos.services.GeminiService;
import com.najmi.oreamnos.services.WebContentExtractor;
import com.najmi.oreamnos.utils.HapticHelper;
import com.najmi.oreamnos.utils.NotificationHelper;
import com.najmi.oreamnos.utils.PreferencesManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bottom Sheet Dialog Fragment for handling shared content.
 * Features: collapsible input, shimmer skeleton loading, tone toggle, haptic
 * feedback,
 * background processing, error state handling.
 */
public class ShareBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_SHARED_TEXT = "shared_text";

    // Views
    private MaterialCardView inputCard;
    private View inputHeader;
    private ImageView expandArrow;
    private TextView sharedText;
    private TextView inputCharCount;
    private View toneToggleContainer;
    private Chip chipFormal;
    private Chip chipCasual;
    private MaterialCardView skeletonCard;
    private ShimmerFrameLayout shimmerLayout;
    private TextView skeletonLoadingText;
    private MaterialCardView errorCard;
    private TextView errorTitle;
    private TextView errorMessage;
    private MaterialButton tryAgainButton;
    private MaterialCardView resultCard;
    private TextInputEditText outputText;
    private TextView outputWordCount;
    private TextView editedIndicator;
    private MaterialButton editButton;
    private MaterialButton copyButton;
    private MaterialButton shareButton;
    private MaterialButton backgroundButton;
    private MaterialButton continueButton;
    private Chip includeTitleCheckbox;
    private Chip includeEmojisCheckbox;
    private Chip includeHashtagsCheckbox;
    private Chip includeSourceCheckbox;
    private TextView readabilityScore;

    // State
    private String originalSharedContent = "";
    private String lastGeneratedPost = "";
    private String generatedSourceCitation = "";
    private String generatedTitle = "";
    private String generatedBody = "";
    private boolean isInputExpanded = true;
    private boolean isEditMode = false;
    private boolean isProcessing = false;

    // Utils
    private PreferencesManager prefsManager;
    private HapticHelper hapticHelper;
    private NotificationHelper notificationHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * BroadcastReceiver for background processing results.
     */
    private final BroadcastReceiver serviceResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(ContentGenerationService.EXTRA_SUCCESS, false);
            if (success) {
                String result = intent.getStringExtra(ContentGenerationService.EXTRA_RESULT);
                handleGenerationSuccess(result);
            } else {
                String error = intent.getStringExtra(ContentGenerationService.EXTRA_ERROR);
                handleGenerationError(error);
            }
        }
    };

    public static ShareBottomSheetFragment newInstance(String sharedText) {
        ShareBottomSheetFragment fragment = new ShareBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SHARED_TEXT, sharedText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_Oreamnos_BottomSheet);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();
        prefsManager = new PreferencesManager(context);
        hapticHelper = new HapticHelper(context);
        notificationHelper = new NotificationHelper(context);

        initViews(view);
        setupListeners();
        loadSharedContent();
        setupToneToggle();
        setupChipsVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                serviceResultReceiver,
                new IntentFilter(ContentGenerationService.BROADCAST_RESULT));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(serviceResultReceiver);
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        // Notify host activity to finish
        if (getActivity() instanceof ShareReceiverActivity) {
            ((ShareReceiverActivity) getActivity()).onBottomSheetDismissed();
        }
    }

    private void initViews(View view) {
        // Input card
        inputCard = view.findViewById(R.id.inputCard);
        inputHeader = view.findViewById(R.id.inputHeader);
        expandArrow = view.findViewById(R.id.expandArrow);
        sharedText = view.findViewById(R.id.sharedText);
        inputCharCount = view.findViewById(R.id.inputCharCount);

        // Tone toggle
        toneToggleContainer = view.findViewById(R.id.toneToggleContainer);
        chipFormal = view.findViewById(R.id.chipFormal);
        chipCasual = view.findViewById(R.id.chipCasual);

        // Skeleton loading (shimmer)
        skeletonCard = view.findViewById(R.id.skeletonCard);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        skeletonLoadingText = view.findViewById(R.id.skeletonLoadingText);

        // Error state
        errorCard = view.findViewById(R.id.errorCard);
        errorTitle = view.findViewById(R.id.errorTitle);
        errorMessage = view.findViewById(R.id.errorMessage);
        tryAgainButton = view.findViewById(R.id.tryAgainButton);

        // Result
        resultCard = view.findViewById(R.id.resultCard);
        outputText = view.findViewById(R.id.outputText);
        outputWordCount = view.findViewById(R.id.outputWordCount);
        editedIndicator = view.findViewById(R.id.editedIndicator);
        editButton = view.findViewById(R.id.editButton);
        copyButton = view.findViewById(R.id.copyButton);
        shareButton = view.findViewById(R.id.shareButton);
        includeTitleCheckbox = view.findViewById(R.id.includeTitleCheckbox);
        includeEmojisCheckbox = view.findViewById(R.id.includeEmojisCheckbox);
        includeHashtagsCheckbox = view.findViewById(R.id.includeHashtagsCheckbox);
        includeSourceCheckbox = view.findViewById(R.id.includeSourceCheckbox);
        readabilityScore = view.findViewById(R.id.readabilityScore);

        // Buttons
        backgroundButton = view.findViewById(R.id.backgroundButton);
        continueButton = view.findViewById(R.id.continueButton);

        // Close button
        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());
    }

    private void setupListeners() {
        // Input card collapse/expand
        inputHeader.setOnClickListener(v -> toggleInputCard());
        inputCard.setOnClickListener(v -> {
            if (!isInputExpanded)
                toggleInputCard();
        });

        // Action buttons
        editButton.setOnClickListener(v -> toggleEditMode());
        copyButton.setOnClickListener(v -> onCopyClick());
        shareButton.setOnClickListener(v -> onShareClick());
        backgroundButton.setOnClickListener(v -> onBackgroundClick());
        continueButton.setOnClickListener(v -> onContinueClick());
        tryAgainButton.setOnClickListener(v -> onTryAgainClick());

        // Output chips
        includeTitleCheckbox.setOnCheckedChangeListener((v, checked) -> rebuildOutputText());
        includeEmojisCheckbox.setOnCheckedChangeListener((v, checked) -> rebuildOutputText());
        includeHashtagsCheckbox.setOnCheckedChangeListener((v, checked) -> rebuildOutputText());
        includeSourceCheckbox.setOnCheckedChangeListener((v, checked) -> rebuildOutputText());

        // Output text watcher
        outputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditMode && !s.toString().equals(lastGeneratedPost)) {
                    editedIndicator.setVisibility(View.VISIBLE);
                } else {
                    editedIndicator.setVisibility(View.GONE);
                }
                String text = s.toString().trim();
                int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;
                outputWordCount.setText(wordCount + " words");

                // Update readability score
                double score = com.najmi.oreamnos.utils.ReadabilityUtils.calculateFleschKincaidGradeLevel(text);
                readabilityScore.setText(String.format("Grade: %.1f", score));
            }
        });
    }

    private void loadSharedContent() {
        if (getArguments() != null) {
            originalSharedContent = getArguments().getString(ARG_SHARED_TEXT, "");
            sharedText.setText(originalSharedContent);
            inputCharCount.setText(originalSharedContent.length() + " characters");

            // Auto-start processing if API key is set
            if (prefsManager.hasApiKey() && !originalSharedContent.isEmpty()) {
                startGeneration();
            } else if (!prefsManager.hasApiKey()) {
                Toast.makeText(getContext(), R.string.api_key_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupToneToggle() {
        // Set initial tone based on settings
        boolean isFormal = prefsManager.isFormalTone();
        chipFormal.setChecked(isFormal);
        chipCasual.setChecked(!isFormal);
    }

    private void setupChipsVisibility() {
        // Hashtags visibility
        boolean hasHashtags = !prefsManager.getHashtags().isEmpty();
        includeHashtagsCheckbox.setVisibility(hasHashtags ? View.VISIBLE : View.GONE);
        includeHashtagsCheckbox.setChecked(prefsManager.areHashtagsEnabled());

        // Source visibility
        boolean isSourceEnabled = prefsManager.isSourceEnabled();
        includeSourceCheckbox.setVisibility(isSourceEnabled ? View.VISIBLE : View.GONE);
        includeSourceCheckbox.setChecked(isSourceEnabled);
    }

    private void toggleInputCard() {
        isInputExpanded = !isInputExpanded;

        TransitionManager.beginDelayedTransition((ViewGroup) inputCard.getParent(), new AutoTransition());

        if (isInputExpanded) {
            sharedText.setVisibility(View.VISIBLE);
            sharedText.setMaxLines(8);
            expandArrow.setRotation(180);
        } else {
            sharedText.setVisibility(View.GONE);
            expandArrow.setRotation(0);
        }
    }

    private void collapseInputCard() {
        if (isInputExpanded) {
            toggleInputCard();
        }
    }

    private void startGeneration() {
        if (isProcessing)
            return;
        isProcessing = true;

        // Haptic feedback
        hapticHelper.onGenerationStart();

        // Show skeleton loading with shimmer
        showLoadingState();

        // Get selected tone
        String tone = chipFormal.isChecked() ? PreferencesManager.TONE_FORMAL : PreferencesManager.TONE_CASUAL;
        boolean includeSource = prefsManager.isSourceEnabled();

        executor.execute(() -> {
            try {
                String textToProcess = originalSharedContent;

                // Check if content is a URL
                if (WebContentExtractor.isUrl(originalSharedContent)) {
                    mainHandler.post(() -> updateLoadingText(getString(R.string.progress_extracting)));
                    WebContentExtractor extractor = new WebContentExtractor();
                    textToProcess = extractor.extractContent(originalSharedContent);
                }

                // Update loading text
                mainHandler.post(() -> updateLoadingText(getString(R.string.progress_generating)));

                // Generate post
                String apiKey = prefsManager.getApiKey();
                String endpoint = prefsManager.getApiEndpoint();
                GeminiService gemini = new GeminiService(apiKey, endpoint, tone);
                String result = gemini.curatePost(textToProcess, includeSource, false);

                mainHandler.post(() -> {
                    mainHandler.postDelayed(() -> handleGenerationSuccess(result), 300);
                });

            } catch (Exception e) {
                mainHandler.post(() -> handleGenerationError(e.getMessage()));
            }
        });
    }

    private void showLoadingState() {
        skeletonCard.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        skeletonLoadingText.setText(R.string.generating_post);
        toneToggleContainer.setVisibility(View.GONE);
        errorCard.setVisibility(View.GONE);
        resultCard.setVisibility(View.GONE);
        backgroundButton.setVisibility(View.VISIBLE);
    }

    private void hideLoadingState() {
        shimmerLayout.stopShimmer();
        skeletonCard.setVisibility(View.GONE);
    }

    private void updateLoadingText(String text) {
        skeletonLoadingText.setText(text);
    }

    private void showErrorState(String error) {
        hideLoadingState();
        errorCard.setVisibility(View.VISIBLE);
        resultCard.setVisibility(View.GONE);
        toneToggleContainer.setVisibility(View.VISIBLE);
        backgroundButton.setVisibility(View.GONE);

        if (error != null && !error.isEmpty()) {
            errorMessage.setText(error);
        } else {
            errorMessage.setText(R.string.error_state_network);
        }
    }

    private void handleGenerationSuccess(String result) {
        isProcessing = false;

        // Haptic feedback
        hapticHelper.onGenerationComplete();

        // Hide loading state
        hideLoadingState();

        // Extract source and title/body
        String contentWithoutSource = extractSourceCitation(result);
        extractTitleAndBody(contentWithoutSource);

        // Rebuild output
        rebuildOutputText();

        // Update UI
        errorCard.setVisibility(View.GONE);
        resultCard.setVisibility(View.VISIBLE);
        backgroundButton.setVisibility(View.GONE);
        continueButton.setVisibility(View.VISIBLE);

        // Collapse input card
        collapseInputCard();
    }

    private void handleGenerationError(String error) {
        isProcessing = false;

        // Haptic feedback
        hapticHelper.onError();

        // Show error state
        showErrorState(error);
    }

    private void onTryAgainClick() {
        // Hide error card and retry generation
        errorCard.setVisibility(View.GONE);
        startGeneration();
    }

    private String extractSourceCitation(String fullResult) {
        if (fullResult == null) {
            generatedSourceCitation = "";
            return "";
        }

        String regex = "(?im)^[\\s\\p{Z}]*[*_]*(?:Sumber|Source)[*_]*[\\s\\p{Z}]*[:：].*$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(fullResult);

        if (matcher.find()) {
            generatedSourceCitation = matcher.group().trim();
            String contentWithoutSource = fullResult.replaceAll(regex, "").trim();
            return contentWithoutSource.replaceAll("\\n+$", "").trim();
        } else {
            generatedSourceCitation = "";
            return fullResult;
        }
    }

    private void extractTitleAndBody(String content) {
        if (content == null || content.isEmpty()) {
            generatedTitle = "";
            generatedBody = "";
            return;
        }

        String[] parts = content.split("\\n\\n", 2);
        if (parts.length >= 2 && parts[0].length() < 150) {
            generatedTitle = parts[0].trim();
            generatedBody = parts[1].trim();
        } else {
            parts = content.split("\\n", 2);
            if (parts.length >= 2 && parts[0].length() < 150) {
                generatedTitle = parts[0].trim();
                generatedBody = parts[1].trim();
            } else {
                generatedTitle = "";
                generatedBody = content.trim();
            }
        }
    }

    private void rebuildOutputText() {
        StringBuilder textBuilder = new StringBuilder();

        // 1. Title (if enabled)
        if (includeTitleCheckbox.isChecked() && !generatedTitle.isEmpty()) {
            String titleText = generatedTitle;
            if (!includeEmojisCheckbox.isChecked()) {
                titleText = com.najmi.oreamnos.utils.StringUtils.stripLeadingEmojis(titleText);
            }
            textBuilder.append(titleText).append("\n\n");
        }

        // 2. Body
        String bodyText = generatedBody;
        if (!includeEmojisCheckbox.isChecked()) {
            // If emojis are disabled, strip them all
            bodyText = com.najmi.oreamnos.utils.StringUtils.stripLeadingEmojis(bodyText);
        } else if (includeTitleCheckbox.isChecked() && !generatedTitle.isEmpty()) {
            // If emojis are enabled BUT title is shown (and has emoji),
            // strip emoji from the body to avoid double emojis.
            // The prompt puts emojis on BOTH Title and First Para, so we remove the Body
            // one here.
            bodyText = com.najmi.oreamnos.utils.StringUtils.stripLeadingEmojis(bodyText);
        }
        // If emojis are enabled AND title is NOT shown, we keep the Body emoji (from
        // the prompt).

        textBuilder.append(bodyText);

        // 3. Source (before hashtags)
        if (includeSourceCheckbox.isChecked() && !generatedSourceCitation.isEmpty()) {
            textBuilder.append("\n\n").append(generatedSourceCitation);
        }

        // 4. Hashtags (last)
        if (includeHashtagsCheckbox.isChecked() && prefsManager.areHashtagsEnabled()) {
            String hashtags = prefsManager.getFormattedHashtags();
            if (!hashtags.isEmpty()) {
                textBuilder.append("\n\n").append(hashtags);
            }
        }

        String finalText = textBuilder.toString().trim();
        lastGeneratedPost = finalText;
        outputText.setText(finalText);
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            outputText.setFocusable(true);
            outputText.setFocusableInTouchMode(true);
            outputText.requestFocus();
            editButton.setText(R.string.save_edit);
            editButton.setIconResource(android.R.drawable.ic_menu_save);
        } else {
            outputText.setFocusable(false);
            outputText.setFocusableInTouchMode(false);
            editButton.setText(R.string.edit_button);
            editButton.setIconResource(android.R.drawable.ic_menu_edit);
            if (outputText.getText() != null) {
                lastGeneratedPost = outputText.getText().toString();
            }
        }
    }

    private String getFinalText() {
        return outputText.getText() != null ? outputText.getText().toString() : "";
    }

    private void onCopyClick() {
        String textToCopy = getFinalText();
        if (textToCopy.isEmpty())
            return;

        hapticHelper.onCopy();

        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Oreamnos Post", textToCopy);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void onShareClick() {
        String textToShare = getFinalText();
        if (textToShare.isEmpty())
            return;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_button)));
    }

    private void onBackgroundClick() {
        // Start background generation service
        String tone = chipFormal.isChecked() ? PreferencesManager.TONE_FORMAL : PreferencesManager.TONE_CASUAL;
        boolean includeSource = prefsManager.isSourceEnabled();

        Intent serviceIntent = new Intent(requireContext(), ContentGenerationService.class);
        serviceIntent.setAction(ContentGenerationService.ACTION_GENERATE);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INPUT_TEXT, originalSharedContent);
        serviceIntent.putExtra(ContentGenerationService.EXTRA_INCLUDE_SOURCE, includeSource);
        serviceIntent.putExtra("tone_override", tone);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
        } else {
            requireContext().startService(serviceIntent);
        }

        Toast.makeText(getContext(), R.string.notification_generating_message, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void onContinueClick() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("shared_text", originalSharedContent);
        intent.putExtra("generated_content", lastGeneratedPost);
        intent.putExtra("generated_title", generatedTitle);
        intent.putExtra("generated_body", generatedBody);
        intent.putExtra("generated_source", generatedSourceCitation);
        startActivity(intent);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
    }
}
