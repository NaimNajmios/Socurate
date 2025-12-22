package com.najmi.oreamnos;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

/**
 * Activity for displaying all logs in a dedicated full-screen list.
 */
public class LogListActivity extends AppCompatActivity {

    private PreferencesManager prefsManager;
    private RecyclerView logsRecyclerView;
    private TextView emptyLogsText;
    private TextView logCountText;
    private MaterialButton clearLogsButton;
    private LogAdapter logAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsManager = new PreferencesManager(this);
        applyTheme(prefsManager.getTheme());

        setContentView(R.layout.activity_log_list);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadLogs();
    }

    private void initViews() {
        logsRecyclerView = findViewById(R.id.logsRecyclerView);
        emptyLogsText = findViewById(R.id.emptyLogsText);
        logCountText = findViewById(R.id.logCountText);
        clearLogsButton = findViewById(R.id.clearLogsButton);

        logsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter();
        logAdapter.setOnLogClickListener(this::showLogDetailsDialog);
        logsRecyclerView.setAdapter(logAdapter);

        clearLogsButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.clear_logs)
                    .setMessage("Are you sure you want to clear all logs?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        prefsManager.clearLogs();
                        loadLogs();
                        Toast.makeText(this, R.string.logs_cleared, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadLogs() {
        UsageStats stats = prefsManager.getUsageStats();
        List<UsageStats.LogEntry> logs = stats.getLogs();

        int count = logs != null ? logs.size() : 0;
        logCountText.setText(String.format(Locale.US, "%d log entries", count));

        if (logs == null || logs.isEmpty()) {
            emptyLogsText.setVisibility(View.VISIBLE);
            logsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyLogsText.setVisibility(View.GONE);
            logsRecyclerView.setVisibility(View.VISIBLE);
            logAdapter.setLogs(logs);
        }
    }

    private void showLogDetailsDialog(UsageStats.LogEntry log) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_log_details, null);

        TextView levelBadge = dialogView.findViewById(R.id.dialogLevelBadge);
        TextView tagText = dialogView.findViewById(R.id.dialogTagText);
        TextView timeText = dialogView.findViewById(R.id.dialogTimeText);
        TextView messageText = dialogView.findViewById(R.id.dialogMessageText);
        TextView detailsText = dialogView.findViewById(R.id.dialogDetailsText);
        MaterialButton copyButton = dialogView.findViewById(R.id.dialogCopyButton);

        levelBadge.setText(log.getLevel());

        int badgeRes;
        switch (log.getLevel()) {
            case UsageStats.LogEntry.LEVEL_ERROR:
                badgeRes = R.drawable.log_badge_error;
                break;
            case UsageStats.LogEntry.LEVEL_WARNING:
                badgeRes = R.drawable.log_badge_warn;
                break;
            case UsageStats.LogEntry.LEVEL_DEBUG:
                badgeRes = R.drawable.log_badge_debug;
                break;
            case UsageStats.LogEntry.LEVEL_INFO:
            default:
                badgeRes = R.drawable.log_badge_info;
                break;
        }
        levelBadge.setBackgroundResource(badgeRes);

        tagText.setText(log.getTag() != null ? log.getTag() : "App");
        timeText.setText(log.getFormattedDate());
        messageText.setText(log.getMessage());

        String details = log.getDetails();
        if (details != null && !details.isEmpty()) {
            detailsText.setText(details);
        } else {
            detailsText.setText("No additional details.");
            detailsText.setTypeface(null, android.graphics.Typeface.ITALIC);
        }

        AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Log Details")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();

        copyButton.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Level: ").append(log.getLevel()).append("\n");
            sb.append("Time: ").append(log.getFormattedDate()).append("\n");
            sb.append("Tag: ").append(log.getTag()).append("\n");
            sb.append("Message: ").append(log.getMessage()).append("\n");
            if (log.getDetails() != null) {
                sb.append("Details:\n").append(log.getDetails());
            }

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Log Details", sb.toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Log details copied", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
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

    // ==================== LOG ADAPTER ====================

    private static class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

        private List<UsageStats.LogEntry> logs;
        private OnLogClickListener listener;

        public interface OnLogClickListener {
            void onLogClick(UsageStats.LogEntry log);
        }

        public void setLogs(List<UsageStats.LogEntry> logs) {
            this.logs = logs;
            notifyDataSetChanged();
        }

        public void setOnLogClickListener(OnLogClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_log_entry, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            if (logs == null || position >= logs.size())
                return;
            UsageStats.LogEntry log = logs.get(position);
            holder.bind(log);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLogClick(log);
                }
            });
        }

        @Override
        public int getItemCount() {
            return logs != null ? logs.size() : 0;
        }

        static class LogViewHolder extends RecyclerView.ViewHolder {
            private final TextView levelBadge;
            private final TextView tagText;
            private final TextView timeText;
            private final TextView messageText;

            LogViewHolder(@NonNull View itemView) {
                super(itemView);
                levelBadge = itemView.findViewById(R.id.levelBadge);
                tagText = itemView.findViewById(R.id.tagText);
                timeText = itemView.findViewById(R.id.timeText);
                messageText = itemView.findViewById(R.id.messageText);
            }

            void bind(UsageStats.LogEntry log) {
                String level = log.getLevel();
                levelBadge.setText(level);

                int badgeRes;
                switch (level) {
                    case UsageStats.LogEntry.LEVEL_ERROR:
                        badgeRes = R.drawable.log_badge_error;
                        break;
                    case UsageStats.LogEntry.LEVEL_WARNING:
                        badgeRes = R.drawable.log_badge_warn;
                        break;
                    case UsageStats.LogEntry.LEVEL_DEBUG:
                        badgeRes = R.drawable.log_badge_debug;
                        break;
                    case UsageStats.LogEntry.LEVEL_INFO:
                    default:
                        badgeRes = R.drawable.log_badge_info;
                        break;
                }
                levelBadge.setBackgroundResource(badgeRes);

                tagText.setText(log.getTag() != null ? log.getTag() : "App");
                timeText.setText(log.getFormattedTime());

                String msg = log.getMessage();
                String details = log.getDetails();
                if (details != null && !details.isEmpty()) {
                    messageText.setText(msg + ": " + details);
                } else {
                    messageText.setText(msg);
                }
            }
        }
    }
}
