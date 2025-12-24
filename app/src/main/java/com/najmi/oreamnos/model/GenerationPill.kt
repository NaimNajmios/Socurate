package com.najmi.oreamnos.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * Represents a Custom Refinement Pill - a user-defined refinement command.
 * Pills appear as selectable chips alongside built-in refinements (Rephrase, etc.).
 */
data class GenerationPill(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var command: String = ""
) {
    /**
     * Secondary constructor for Java compatibility (name and command only).
     */
    constructor(name: String, command: String) : this(
        id = UUID.randomUUID().toString(),
        name = name,
        command = command
    )

    companion object {
        /**
         * Converts a list of pills to JSON string.
         */
        @JvmStatic
        fun toJson(pills: List<GenerationPill>): String = Gson().toJson(pills)

        /**
         * Parses JSON string to list of pills.
         */
        @JvmStatic
        fun fromJson(json: String?): List<GenerationPill> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                val listType = object : TypeToken<List<GenerationPill>>() {}.type
                Gson().fromJson<List<GenerationPill>>(json, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
