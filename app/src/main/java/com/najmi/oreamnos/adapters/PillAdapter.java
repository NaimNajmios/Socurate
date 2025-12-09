package com.najmi.oreamnos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.najmi.oreamnos.R;
import com.najmi.oreamnos.model.GenerationPill;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying Generation Pills in settings.
 */
public class PillAdapter extends RecyclerView.Adapter<PillAdapter.PillViewHolder> {

    private List<GenerationPill> pills = new ArrayList<>();
    private String activePillId = null;
    private OnPillActionListener listener;

    public interface OnPillActionListener {
        void onPillClick(GenerationPill pill);

        void onPillEdit(GenerationPill pill);

        void onPillDelete(GenerationPill pill);

        void onPillSetActive(GenerationPill pill);
    }

    public PillAdapter(OnPillActionListener listener) {
        this.listener = listener;
    }

    public void setPills(List<GenerationPill> pills) {
        this.pills = pills != null ? new ArrayList<>(pills) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setActivePillId(String activePillId) {
        this.activePillId = activePillId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pill_settings, parent, false);
        return new PillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PillViewHolder holder, int position) {
        GenerationPill pill = pills.get(position);
        holder.bind(pill, activePillId != null && activePillId.equals(pill.getId()));
    }

    @Override
    public int getItemCount() {
        return pills.size();
    }

    class PillViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView nameText;
        private final TextView summaryText;
        private final Chip toneChip;
        private final Chip activeChip;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        PillViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.pillCard);
            nameText = itemView.findViewById(R.id.pillName);
            summaryText = itemView.findViewById(R.id.pillSummary);
            toneChip = itemView.findViewById(R.id.toneChip);
            activeChip = itemView.findViewById(R.id.activeChip);
            editButton = itemView.findViewById(R.id.editPillButton);
            deleteButton = itemView.findViewById(R.id.deletePillButton);
        }

        void bind(GenerationPill pill, boolean isActive) {
            nameText.setText(pill.getName());
            summaryText.setText(pill.getOptionsSummary());

            // Set tone chip
            if (pill.getTone() != null) {
                toneChip.setVisibility(View.VISIBLE);
                toneChip.setText(pill.getTone().substring(0, 1).toUpperCase()
                        + pill.getTone().substring(1));
            } else {
                toneChip.setVisibility(View.GONE);
            }

            // Show active indicator
            activeChip.setVisibility(isActive ? View.VISIBLE : View.GONE);

            // Highlight active card
            if (isActive) {
                cardView.setStrokeWidth(4);
            } else {
                cardView.setStrokeWidth(1);
            }

            // Click handlers
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPillSetActive(pill);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPillEdit(pill);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPillDelete(pill);
                }
            });
        }
    }
}
