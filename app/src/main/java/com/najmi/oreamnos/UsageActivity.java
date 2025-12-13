package com.najmi.oreamnos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.najmi.oreamnos.model.UsageStats;
import com.najmi.oreamnos.utils.PreferencesManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for displaying comprehensive API usage statistics.
 * Shows time-based, per-provider, per-model stats and recent activity.
 */
public class UsageActivity extends AppCompatActivity {

    private static final String TAG = "UsageActivity";

    private PreferencesManager prefsManager;

    // Overview
    private TextView totalTokensValue;
    private TextView successRateValue;
    private TextView requestsCount;

    // Time-based
    private TextView todayTokens;
    private TextView todayRequests;
    private TextView weekTokens;
    private TextView weekRequests;
    private TextView monthTokens;
    private TextView monthRequests;

    // Token breakdown
    private View promptTokensBar;
    private View responseTokensBar;
    private TextView promptTokensValue;
    private TextView responseTokensValue;

    // Provider stats
    private ProgressBar geminiProgressBar;
    private TextView geminiTokens;
    private ProgressBar groqProgressBar;
    private TextView groqTokens;
    private ProgressBar openRouterProgressBar;
    private TextView openRouterTokens;

    // Request stats
    private TextView successfulRequestsValue;
    private TextView failedRequestsValue;

    // Sessions
    private RecyclerView sessionsRecyclerView;
    private TextView emptySessionsText;
    private SessionAdapter sessionAdapter;

    // Reset button
    private MaterialButton resetStatsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsManager = new PreferencesManager(this);
        applyTheme(prefsManager.getTheme());

        setContentView(R.layout.activity_usage);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        setupResetButton();
        refreshStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStats();
    }

    private void initViews() {
        // Overview
        totalTokensValue = findViewById(R.id.totalTokensValue);
        successRateValue = findViewById(R.id.successRateValue);
        requestsCount = findViewById(R.id.requestsCount);

        // Time-based
        todayTokens = findViewById(R.id.todayTokens);
        todayRequests = findViewById(R.id.todayRequests);
        weekTokens = findViewById(R.id.weekTokens);
        weekRequests = findViewById(R.id.weekRequests);
        monthTokens = findViewById(R.id.monthTokens);
        monthRequests = findViewById(R.id.monthRequests);

        // Token breakdown
        promptTokensBar = findViewById(R.id.promptTokensBar);
        responseTokensBar = findViewById(R.id.responseTokensBar);
        promptTokensValue = findViewById(R.id.promptTokensValue);
        responseTokensValue = findViewById(R.id.responseTokensValue);

        // Provider stats
        geminiProgressBar = findViewById(R.id.geminiProgressBar);
        geminiTokens = findViewById(R.id.geminiTokens);
        groqProgressBar = findViewById(R.id.groqProgressBar);
        groqTokens = findViewById(R.id.groqTokens);
        openRouterProgressBar = findViewById(R.id.openRouterProgressBar);
        openRouterTokens = findViewById(R.id.openRouterTokens);

        // Request stats
        successfulRequestsValue = findViewById(R.id.successfulRequestsValue);
        failedRequestsValue = findViewById(R.id.failedRequestsValue);

        // Sessions
        sessionsRecyclerView = findViewById(R.id.sessionsRecyclerView);
        emptySessionsText = findViewById(R.id.emptySessionsText);
        sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionAdapter = new SessionAdapter();
        sessionsRecyclerView.setAdapter(sessionAdapter);

        // Reset button
        resetStatsButton = findViewById(R.id.resetStatsButton);
    }

    private void setupResetButton() {
        resetStatsButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reset_stats)
                    .setMessage("Are you sure you want to reset all usage statistics? This cannot be undone.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        prefsManager.resetUsageStats();
                        refreshStats();
                        Toast.makeText(this, R.string.stats_reset, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void refreshStats() {
        UsageStats stats = prefsManager.getUsageStats();

        // Overview
        totalTokensValue.setText(formatNumber(stats.getTotalTokens()));
        successRateValue.setText(String.format(Locale.US, "%.0f%%", stats.getSuccessRate()));
        requestsCount.setText(String.format(Locale.US, "%d requests", stats.getTotalRequests()));

        // Time-based
        todayTokens.setText(formatNumber(stats.getTodayTokens()));
        todayRequests.setText(String.format(Locale.US, "%d req", stats.getTodayRequests()));
        weekTokens.setText(formatNumber(stats.getWeekTokens()));
        weekRequests.setText(String.format(Locale.US, "%d req", stats.getWeekRequests()));
        monthTokens.setText(formatNumber(stats.getMonthTokens()));
        monthRequests.setText(String.format(Locale.US, "%d req", stats.getMonthRequests()));

        // Token breakdown
        updateTokenBreakdown(stats);

        // Provider stats
        updateProviderStats(stats);

        // Request stats
        successfulRequestsValue.setText(String.valueOf(stats.getSuccessfulRequests()));
        failedRequestsValue.setText(String.valueOf(stats.getFailedRequests()));

        // Sessions
        updateSessionsList(stats);
    }

    private void updateTokenBreakdown(UsageStats stats) {
        promptTokensValue.setText(String.format(Locale.US, "Prompt: %s", formatNumber(stats.getTotalPromptTokens())));
        responseTokensValue
                .setText(String.format(Locale.US, "Response: %s", formatNumber(stats.getTotalCandidateTokens())));

        long promptTokens = stats.getTotalPromptTokens();
        long responseTokens = stats.getTotalCandidateTokens();
        long totalTokens = promptTokens + responseTokens;

        if (totalTokens > 0) {
            float promptWeight = (float) promptTokens / totalTokens;
            float responseWeight = (float) responseTokens / totalTokens;

            android.widget.LinearLayout.LayoutParams promptParams = (android.widget.LinearLayout.LayoutParams) promptTokensBar
                    .getLayoutParams();
            promptParams.weight = promptWeight;
            promptTokensBar.setLayoutParams(promptParams);

            android.widget.LinearLayout.LayoutParams responseParams = (android.widget.LinearLayout.LayoutParams) responseTokensBar
                    .getLayoutParams();
            responseParams.weight = responseWeight;
            responseTokensBar.setLayoutParams(responseParams);
        } else {
            android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) promptTokensBar
                    .getLayoutParams();
            params.weight = 0.5f;
            promptTokensBar.setLayoutParams(params);

            params = (android.widget.LinearLayout.LayoutParams) responseTokensBar.getLayoutParams();
            params.weight = 0.5f;
            responseTokensBar.setLayoutParams(params);
        }
    }

    private void updateProviderStats(UsageStats stats) {
        Map<String, UsageStats.ProviderStats> providerStats = stats.getProviderStats();
        long totalTokens = stats.getTotalTokens();

        // Gemini
        UsageStats.ProviderStats geminiStats = providerStats.get("gemini");
        long geminiTotal = geminiStats != null ? geminiStats.getTotalTokens() : 0;
        geminiTokens.setText(formatNumber(geminiTotal));
        geminiProgressBar.setProgress(totalTokens > 0 ? (int) (geminiTotal * 100 / totalTokens) : 0);

        // Groq
        UsageStats.ProviderStats groqStats = providerStats.get("groq");
        long groqTotal = groqStats != null ? groqStats.getTotalTokens() : 0;
        groqTokens.setText(formatNumber(groqTotal));
        groqProgressBar.setProgress(totalTokens > 0 ? (int) (groqTotal * 100 / totalTokens) : 0);

        // OpenRouter
        UsageStats.ProviderStats orStats = providerStats.get("openrouter");
        long orTotal = orStats != null ? orStats.getTotalTokens() : 0;
        openRouterTokens.setText(formatNumber(orTotal));
        openRouterProgressBar.setProgress(totalTokens > 0 ? (int) (orTotal * 100 / totalTokens) : 0);
    }

    private void updateSessionsList(UsageStats stats) {
        List<UsageStats.SessionEntry> sessions = stats.getRecentSessions();

        if (sessions == null || sessions.isEmpty()) {
            emptySessionsText.setVisibility(View.VISIBLE);
            sessionsRecyclerView.setVisibility(View.GONE);
        } else {
            emptySessionsText.setVisibility(View.GONE);
            sessionsRecyclerView.setVisibility(View.VISIBLE);
            sessionAdapter.setSessions(sessions);
        }
    }

    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format(Locale.US, "%.1fK", number / 1_000.0);
        }
        return String.format(Locale.US, "%,d", number);
    }

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

    // ==================== SESSION ADAPTER ====================

    private static class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

        private List<UsageStats.SessionEntry> sessions;

        public void setSessions(List<UsageStats.SessionEntry> sessions) {
            this.sessions = sessions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session_entry, parent, false);
            return new SessionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
            if (sessions == null || position >= sessions.size())
                return;

            UsageStats.SessionEntry session = sessions.get(position);
            holder.bind(session);
        }

        @Override
        public int getItemCount() {
            return sessions != null ? sessions.size() : 0;
        }

        static class SessionViewHolder extends RecyclerView.ViewHolder {
            private final View statusIndicator;
            private final TextView providerModelText;
            private final TextView timestampText;
            private final TextView tokensText;

            SessionViewHolder(@NonNull View itemView) {
                super(itemView);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
                providerModelText = itemView.findViewById(R.id.providerModelText);
                timestampText = itemView.findViewById(R.id.timestampText);
                tokensText = itemView.findViewById(R.id.tokensText);
            }

            void bind(UsageStats.SessionEntry session) {
                // Status indicator color
                if (session.isSuccess()) {
                    statusIndicator.setBackgroundResource(R.drawable.circle_indicator);
                } else {
                    statusIndicator.setBackgroundColor(0xFFEA4335); // Red for failure
                }

                // Provider and model
                String provider = session.getProvider() != null ? capitalize(session.getProvider()) : "Unknown";
                String model = session.getModelName() != null ? session.getModelName() : "";

                if (!model.isEmpty()) {
                    providerModelText.setText(String.format("%s • %s", provider, model));
                } else {
                    providerModelText.setText(provider);
                }

                // Timestamp
                timestampText.setText(session.getFormattedTime());

                // Tokens
                if (session.isSuccess()) {
                    tokensText.setText(String.format(Locale.US, "%,d tokens", session.getTotalTokens()));
                    tokensText.setTextColor(itemView.getContext().getResources()
                            .getColor(android.R.color.holo_green_dark, itemView.getContext().getTheme()));
                } else {
                    tokensText.setText("Failed");
                    tokensText.setTextColor(0xFFEA4335);
                }
            }

            private String capitalize(String s) {
                if (s == null || s.isEmpty())
                    return s;
                return s.substring(0, 1).toUpperCase() + s.substring(1);
            }
        }
    }
}
