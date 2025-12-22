package com.najmi.oreamnos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.najmi.oreamnos.model.UsageStats;
import com.najmi.oreamnos.utils.PreferencesManager;

import java.util.List;
import java.util.Locale;

/**
 * Activity for displaying all sessions in a dedicated full-screen list.
 */
public class SessionListActivity extends AppCompatActivity {

    private PreferencesManager prefsManager;
    private RecyclerView sessionsRecyclerView;
    private TextView emptySessionsText;
    private TextView sessionCountText;
    private SessionAdapter sessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsManager = new PreferencesManager(this);
        applyTheme(prefsManager.getTheme());

        setContentView(R.layout.activity_session_list);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadSessions();
    }

    private void initViews() {
        sessionsRecyclerView = findViewById(R.id.sessionsRecyclerView);
        emptySessionsText = findViewById(R.id.emptySessionsText);
        sessionCountText = findViewById(R.id.sessionCountText);

        sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionAdapter = new SessionAdapter();
        sessionsRecyclerView.setAdapter(sessionAdapter);
    }

    private void loadSessions() {
        UsageStats stats = prefsManager.getUsageStats();
        List<UsageStats.SessionEntry> sessions = stats.getRecentSessions();

        int count = sessions != null ? sessions.size() : 0;
        sessionCountText.setText(String.format(Locale.US, "%d sessions", count));

        if (sessions == null || sessions.isEmpty()) {
            emptySessionsText.setVisibility(View.VISIBLE);
            sessionsRecyclerView.setVisibility(View.GONE);
        } else {
            emptySessionsText.setVisibility(View.GONE);
            sessionsRecyclerView.setVisibility(View.VISIBLE);
            sessionAdapter.setSessions(sessions);
        }
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
            private final TextView tokensChip;

            SessionViewHolder(@NonNull View itemView) {
                super(itemView);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
                providerModelText = itemView.findViewById(R.id.providerModelText);
                timestampText = itemView.findViewById(R.id.timestampText);
                tokensChip = itemView.findViewById(R.id.tokensChip);
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
                    tokensChip.setText(String.format(Locale.US, "%,d", session.getTotalTokens()));
                } else {
                    tokensChip.setText("Failed");
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
