## 2024-05-23 - [Null Safety/Integration]

**Context:** `GenerationPill.kt` data class used for custom refinement options.
**Symptoms:** Potential `NullPointerException` at runtime when accessing non-nullable fields (`id`, `name`, `command`) if the backing JSON source is malformed or missing fields.
**Root Cause:** Gson uses `UnsafeAllocator` to instantiate classes, which bypasses Kotlin's constructor and property initializers. If a field is missing in the JSON, it remains `null` even if the Kotlin property is non-nullable (`String`).
**Fix Applied:** Implemented a custom `JsonDeserializer` that manually parses the JSON object. It checks for the existence and non-nullity of each field and provides the correct default values (including generating a new UUID for `id`) if data is missing. The `Gson` instance is cached in a `lazy` companion object property for efficiency.
**Prevention:** Always use custom deserializers or Moshi (which supports Kotlin defaults natively) when dealing with external JSON sources that might not perfectly match the Kotlin data model.
**Tests Added:** Verified via code review and pattern matching against known Gson behavior. (Unit tests blocked by environment constraints).

**Code Example (Before):**
```kotlin
data class GenerationPill(
    var id: String = UUID.randomUUID().toString(), // Ignored by Gson if missing in JSON
    var name: String = ""
)
// Result: id is null at runtime if JSON is "{}"
```

**Code Example (After):**
```kotlin
private class Deserializer : JsonDeserializer<GenerationPill> {
    override fun deserialize(...): GenerationPill {
        val id = if (obj.has("id")) obj.get("id").asString else UUID.randomUUID().toString()
        // ...
        return GenerationPill(id, ...)
    }
}
// Result: id is never null
```
