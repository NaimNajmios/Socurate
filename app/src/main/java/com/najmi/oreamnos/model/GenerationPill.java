package com.najmi.oreamnos.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Generation Pill - a preset combination of generation options.
 * Pills allow users to quickly apply saved settings like tone, refinements,
 * and custom instructions when generating posts.
 */
public class GenerationPill {

    private String id;
    private String name;
    private String tone; // "formal" or "casual"
    private List<String> refinements;
    private String customInstruction;

    // Required for Gson deserialization
    public GenerationPill() {
        this.id = UUID.randomUUID().toString();
        this.refinements = new ArrayList<>();
    }

    public GenerationPill(String name, String tone) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.tone = tone;
        this.refinements = new ArrayList<>();
        this.customInstruction = "";
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTone() {
        return tone;
    }

    public List<String> getRefinements() {
        return refinements != null ? refinements : new ArrayList<>();
    }

    public String getCustomInstruction() {
        return customInstruction;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public void setRefinements(List<String> refinements) {
        this.refinements = refinements != null ? refinements : new ArrayList<>();
    }

    public void setCustomInstruction(String customInstruction) {
        this.customInstruction = customInstruction;
    }

    /**
     * Returns a display-friendly summary of the pill's options.
     */
    public String getOptionsSummary() {
        StringBuilder summary = new StringBuilder();

        // Add tone
        if (tone != null && !tone.isEmpty()) {
            summary.append(tone.substring(0, 1).toUpperCase())
                    .append(tone.substring(1));
        }

        // Add refinement count
        int refCount = getRefinements().size();
        if (refCount > 0) {
            if (summary.length() > 0)
                summary.append(" • ");
            summary.append(refCount).append(" refinement");
            if (refCount > 1)
                summary.append("s");
        }

        // Indicate custom instruction
        if (customInstruction != null && !customInstruction.trim().isEmpty()) {
            if (summary.length() > 0)
                summary.append(" • ");
            summary.append("Custom");
        }

        return summary.length() > 0 ? summary.toString() : "Default settings";
    }

    /**
     * Converts a list of pills to JSON string.
     */
    public static String toJson(List<GenerationPill> pills) {
        return new Gson().toJson(pills);
    }

    /**
     * Parses JSON string to list of pills.
     */
    public static List<GenerationPill> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<List<GenerationPill>>() {
            }.getType();
            List<GenerationPill> pills = new Gson().fromJson(json, listType);
            return pills != null ? pills : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GenerationPill that = (GenerationPill) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
