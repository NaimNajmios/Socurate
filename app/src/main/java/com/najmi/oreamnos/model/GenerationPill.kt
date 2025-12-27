package com.najmi.oreamnos.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
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
        // Cache the Gson instance with the custom adapter to avoid recreation overhead
        private val gson: Gson by lazy {
            GsonBuilder()
                .registerTypeAdapter(GenerationPill::class.java, Deserializer())
                .create()
        }

        /**
         * Converts a list of pills to JSON string.
         */
        @JvmStatic
        fun toJson(pills: List<GenerationPill>): String {
            return gson.toJson(pills)
        }

        /**
         * Parses JSON string to list of pills.
         * Uses a custom deserializer to ensure default values are respected if fields are missing.
         */
        @JvmStatic
        fun fromJson(json: String?): List<GenerationPill> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                val listType = object : TypeToken<List<GenerationPill>>() {}.type
                gson.fromJson<List<GenerationPill>>(json, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * Custom Deserializer to handle missing fields in JSON.
         * Ensures that non-nullable fields like 'id' are properly initialized with defaults
         * even if the JSON object is empty or partial.
         */
        private class Deserializer : JsonDeserializer<GenerationPill> {
            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): GenerationPill {
                val obj = json.asJsonObject

                // Safely extract fields, falling back to defaults if missing or null
                val id = if (obj.has("id") && !obj.get("id").isJsonNull) {
                    obj.get("id").asString
                } else {
                    UUID.randomUUID().toString()
                }

                val name = if (obj.has("name") && !obj.get("name").isJsonNull) {
                    obj.get("name").asString
                } else {
                    ""
                }

                val command = if (obj.has("command") && !obj.get("command").isJsonNull) {
                    obj.get("command").asString
                } else {
                    ""
                }

                return GenerationPill(id, name, command)
            }
        }
    }
}
