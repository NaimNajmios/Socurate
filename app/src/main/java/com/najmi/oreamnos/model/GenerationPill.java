package com.najmi.oreamnos.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Custom Refinement Pill - a user-defined refinement command.
 * Pills appear as selectable chips alongside built-in refinements (Rephrase,
 * etc.).
 */
public class GenerationPill {

    private String id;
    private String name; // Display label (e.g., "Punchy")
    private String command; // AI instruction (e.g., "Make it punchy and engaging")

    // Required for Gson deserialization
    public GenerationPill() {
        this.id = UUID.randomUUID().toString();
    }

    public GenerationPill(String name, String command) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.command = command;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCommand(String command) {
        this.command = command;
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
