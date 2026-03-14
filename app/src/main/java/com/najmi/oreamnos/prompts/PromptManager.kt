package com.najmi.oreamnos.prompts

/**
 * Centralizes prompt engineering for content curation.
 * Extracted from GeminiService for reusability across different AI providers.
 *
 * OPTIMIZATION: Converted to object (singleton) to avoid object allocation on every API request.
 */
object PromptManager {

    /**
     * Builds the initial curation prompt based on tone and input text.
     * Detects quotes and long/technical content to adapt the prompt.
     *
     * @param tone          Post tone ("formal" or "casual")
     * @param inputText     The text to curate
     * @param includeSource Whether to include source citation
     * @param keepStructure Whether to preserve original formatting/structure
     * @return The formatted prompt string
     */
    fun buildInitialPrompt(
        inputText: String,
        includeSource: Boolean,
        keepStructure: Boolean,
        length: String? = "medium"
    ): String {
        val originalLength = inputText.length
        var targetMinLength = (originalLength * 0.4).toInt()
        var targetMaxLength = (originalLength * 0.6).toInt()

        // Ensure reasonable defaults if text is short
        if (targetMinLength < 50) targetMinLength = 50
        if (targetMaxLength < 100) targetMaxLength = 100

        // Detect quotes in input
        val hasQuotes = containsQuotes(inputText)

        // Detect if original has bullet points/lists
        val hasBulletPoints = containsBulletPoints(inputText)

        // Detect long/technical content
        val isTechnicalArticle = isLongTechnicalContent(inputText)

        // Build base prompt
        val prompt = StringBuilder()
        prompt.append(
            "You are a professional social media content writer for a Malaysian football club. Your task is to transform the following English football news article into a professional social media post written in Malaysian Malay (Bahasa Malaysia).\n\n"
        )

        prompt.append("STRICT REQUIREMENTS:\n")
            .append("1. Write in Bahasa Malaysia (Malaysian Malay), BUT ALWAYS use accepted English football terms instead of making up stiff direct translations. Do NOT translate: 'Clean Sheet', 'Offside', 'Hat-trick', 'Tackle', 'Assist', 'Playmaker', 'Derby', 'Comeback', 'Winger', 'Striker', 'Midfielder', 'Defender', 'Full-back', 'Center-back', 'Goalkeeper', 'Free-kick', 'Penalty', 'Corner Kicks', 'VAR', 'Counter-attack', 'Pressing', 'Cross', 'Header', 'Nutmeg', 'Dribble', 'Volley', 'Bicycle Kick', 'Man of the Match', 'Golden Boot', 'Pitch', 'Box-to-box', 'Sweeper', 'Target Man', 'False Nine', 'High Press', 'Through Ball', 'Overhead Kick'.\n")
            .append("2. Maintain a professional and authoritative Malaysian Malay (Bahasa Malaysia) specifically for a football club's voice.\n")

        if (keepStructure) {
            prompt.append(
                "3. STRICTLY PRESERVE the original formatting, bullet points, lists, and structure. Do NOT summarize into paragraphs if the original used a list format. Translate the content line-by-line while keeping the visual layout exactly the same.\n"
            )
        } else {
            val lengthMultiplier = when (length) {
                "short" -> 0.2 to 0.3
                "long" -> 0.7 to 0.9
                else -> 0.4 to 0.6 // medium (default)
            }
            targetMinLength = (originalLength * lengthMultiplier.first).toInt().coerceAtLeast(50)
            targetMaxLength = (originalLength * lengthMultiplier.second).toInt().coerceAtLeast(100)

            prompt.append("3. The output MUST be ")
                .append(if (length == "short") "concise and brief" else if (length == "long") "detailed and comprehensive" else "moderate in length")
                .append(" (approximately ").append((lengthMultiplier.first * 100).toInt()).append("-").append((lengthMultiplier.second * 100).toInt())
                .append("% of original, target: ").append(targetMinLength).append("-").append(targetMaxLength).append(" characters).\n")
        }

        // Add quote handling instruction if quotes detected
        if (hasQuotes) {
            prompt.append(
                "4. QUOTE HANDLING: If the original text contains quotes, you MUST translate them directly into Bahasa Malaysia. Do NOT paraphrase or turn quotes into normal phrases. Maintain the conversational tone of the quote - not too formal, not too laid back.\n"
            )
            prompt.append(
                "5. FORBIDDEN: Do not use personal commentary phrases like \"Saya cuba\", \"Saya rasa\", \"Pada pendapat saya\"\n"
            )
            prompt.append("6. FORBIDDEN: Do not use em-dashes (—) anywhere in the output\n")
            prompt.append("7. FORBIDDEN: Do NOT include any hashtags in the output\n")
        } else {
            prompt.append(
                "4. FORBIDDEN: Do not use personal commentary phrases like \"Saya cuba\", \"Saya rasa\", \"Pada pendapat saya\"\n"
            )
            prompt.append("5. FORBIDDEN: Do not use em-dashes (—) anywhere in the output\n")
            prompt.append("6. FORBIDDEN: Do NOT include any hashtags in the output\n")
        }

        if (!includeSource) {
            prompt.append("8. FORBIDDEN: Do NOT include any 'Sumber:' citation in the output\n")
        }

        // Adapt structure based on content type
        if (!keepStructure) {
            val structureNum = if (hasQuotes) "8" else "7"
            if (isTechnicalArticle) {
                prompt.append(structureNum)
                    .append(". STRUCTURE FOR TECHNICAL ANALYSIS: Start with a clear, engaging Headline. Then organize content focusing on:\n")
                    .append("   - Key Stats: Highlight important statistics and numbers\n")
                    .append("   - Formations: Describe tactical setups and player positions\n")
                    .append("   - Tactical Shifts: Explain strategic changes and their impact\n")
                    .append("   Separate sections with blank lines.\n")
            } else {
                prompt.append(structureNum)
                    .append(". STRUCTURE: Start with a clear, engaging Headline. Separate paragraphs with a blank line.\n")
            }
        }

        var nextNum = if (isTechnicalArticle || hasQuotes) 9 else 8
        if (hasQuotes && isTechnicalArticle) nextNum = 10

        prompt.append(nextNum).append(". Preserve key facts, names, dates, and statistics from the original\n")
        prompt.append(nextNum + 1).append(". Make the content engaging but maintain journalistic objectivity\n")
        prompt.append(nextNum + 2).append(
            ". Do NOT include any emojis anywhere in the output.\n"
        )
        if (hasBulletPoints) {
            prompt.append(nextNum + 3).append(
                ". The original content contains bullet points/lists - preserve this format using the • character only.\n"
            )
        } else {
            prompt.append(nextNum + 3).append(
                ". Do NOT use bullet points or lists. Write in flowing paragraph format only.\n"
            )
        }
        prompt.append(nextNum + 4)
            .append(". The tone should be that of an official club announcement or news update\n\n")

        prompt.append("ORIGINAL ENGLISH TEXT:\n---\n")
            .append(inputText).append("\n---\n\n")

        when {
            keepStructure -> prompt.append(
                "Provide ONLY the Bahasa Malaysia social media post. STRICTLY PRESERVE the original formatting (lists, bullets, spacing). Use • for any bullet points. Do NOT include any hashtags or emojis."
            )
            isTechnicalArticle && hasBulletPoints -> prompt.append(
                "Provide ONLY the Bahasa Malaysia social media post. Structure it with a headline followed by Key Stats, Formations, and Tactical Shifts sections. Use • for bullet points in lists. Separate sections with blank lines. Do NOT include any hashtags or emojis."
            )
            isTechnicalArticle -> prompt.append(
                "Provide ONLY the Bahasa Malaysia social media post. Structure it with a headline followed by Key Stats, Formations, and Tactical Shifts paragraphs. Write in flowing paragraph format, do NOT use bullet points. Separate sections with blank lines. Do NOT include any hashtags or emojis."
            )
            hasBulletPoints -> prompt.append(
                "Provide ONLY the Bahasa Malaysia social media post. Ensure the output is structured with a headline and paragraphs separated by blank lines. Use • for any bullet points from the original. Do NOT include any hashtags or emojis."
            )
            else -> prompt.append(
                "Provide ONLY the Bahasa Malaysia social media post. Ensure the output is structured with a headline and paragraphs separated by blank lines. Do NOT use bullet points or lists - write in paragraph format only. Do NOT include any hashtags or emojis."
            )
        }

        if (includeSource) {
            prompt.append(
                "\n\nREMEMBER: End your post with a new line containing 'Sumber: [Source Name]' where Source Name is the website, publication, or journalist identified from the content."
            )
        } else {
            prompt.append(
                "\n\nREMEMBER: Do NOT include any 'Sumber:' citation in the output. Do NOT mention the source name, publication, or author anywhere in the post."
            )
        }

        return prompt.toString()
    }

    /**
     * Builds a prompt specifically for text extracted via OCR from screenshots.
     * Tells the AI to interpret the data as football stats or match details.
     *
     * @param ocrText   The text extracted from the screenshot
     * @param tone      Post tone ("formal" or "casual")
     * @param hashtags  Formatted hashtags to include (if any)
     * @return The formatted prompt string
     */
    fun buildPromptFromOcr(ocrText: String, hashtags: String): String {
        val prompt = StringBuilder()
        
        prompt.append("You are a professional social media content writer for a Malaysian football club. ")
        prompt.append("The following text was extracted via OCR from a matchday/stats screenshot. ")
        prompt.append("Your task is to interpret this technical data and generate an engaging ")
        prompt.append("professional social media post in Malaysian Malay (Bahasa Malaysia) for our fans.\n\n")
        
        prompt.append("STRICT REQUIREMENTS:\n")
        prompt.append("1. Write in Bahasa Malaysia (Malaysian Malay).\n")
        prompt.append("2. Use standard English football terms for technical actions (e.g., 'Clean Sheet', 'Hat-trick', 'Assist', 'Tackle').\n")
        prompt.append("3. Present the stats or match result in a clear, exciting way.\n")
        prompt.append("4. Do NOT use em-dashes (—).\n")
        prompt.append("5. Do NOT include any hashtags in the body.\n\n")

        prompt.append("EXTRACTED OCR DATA:\n---\n")
        prompt.append(ocrText).append("\n---\n\n")
        
        prompt.append("Provide ONLY the Bahasa Malaysia post. ")
        if (hashtags.isNotEmpty()) {
            prompt.append("After the post, add a double newline and append these hashtags: ").append(hashtags)
        }
        
        return prompt.toString()
    }

    /**
     * Builds a prompt specifically for text extracted via Vision AI (Gemini Nano, PaliGemma, Gemma 3).
     * Vision models return structured labeled data, so the prompt instructs the AI to format it into a post.
     *
     * @param structuredText The structured text extracted from the vision model
     * @param hashtags      Formatted hashtags to include (if any)
     * @return The formatted prompt string
     */
    fun buildPromptFromVisionExtraction(structuredText: String, hashtags: String): String {
        val prompt = StringBuilder()
        
        prompt.append("You are a professional social media content writer for a Malaysian football club. ")
        prompt.append("The following data was extracted via AI vision from a football screenshot. ")
        prompt.append("Your task is to transform this structured data into an engaging ")
        prompt.append("professional social media post in Malaysian Malay (Bahasa Malaysia) for our fans.\n\n")
        
        prompt.append("STRICT REQUIREMENTS:\n")
        prompt.append("1. Write in Bahasa Malaysia (Malaysian Malay).\n")
        prompt.append("2. Use standard English football terms for technical actions (e.g., 'Clean Sheet', 'Hat-trick', 'Assist', 'Tackle', 'Offside', 'Derby').\n")
        prompt.append("3. Make the post exciting and engaging - celebrate wins, acknowledge good performances.\n")
        prompt.append("4. Do NOT use em-dashes (—).\n")
        prompt.append("5. Do NOT include any hashtags in the body.\n")
        prompt.append("6. The extracted data may contain labeled fields like 'Match:', 'Score:', 'Player:', etc. - interpret and present this data naturally in the post.\n\n")

        prompt.append("EXTRACTED VISION DATA:\n---\n")
        prompt.append(structuredText).append("\n---\n\n")
        
        prompt.append("Provide ONLY the Bahasa Malaysia post. ")
        if (hashtags.isNotEmpty()) {
            prompt.append("After the post, add a double newline and append these hashtags: ").append(hashtags)
        }
        
        return prompt.toString()
    }

    /**
     * Builds a refinement prompt based on selected options.
     *
     * @param originalPost  The original post to refine
     * @param refinements   List of refinement options
     * @param includeSource Whether to include source citation
     * @return The formatted refinement prompt
     */
    fun buildRefinementPrompt(originalPost: String, refinements: List<String>, includeSource: Boolean): String {
        val prompt = StringBuilder()
        prompt.append("You are refining a Malaysian Malay (Bahasa Malaysia) social media post about football. ")
        prompt.append("Apply the following improvements to the post:\n\n")

        for (refinement in refinements) {
            when (refinement) {
                "rephrase" -> prompt.append(
                    "- Rephrase: Rewrite the post with different wording while maintaining the same meaning and facts\n"
                )
                "recheck_flow" -> prompt.append(
                    "- Recheck Flow: Improve the logical flow and structure of ideas\n"
                )
                "recheck_wording" -> prompt.append(
                    "- Recheck Wording: Improve word choice and phrasing for better clarity\n"
                )
                else -> {
                    // Handle custom refinement commands (user-defined pills)
                    if (!refinement.isNullOrBlank()) {
                        prompt.append("- Custom Instruction: ").append(refinement).append("\n")
                    }
                }
            }
        }

        prompt.append("\nORIGINAL POST:\n---\n")
        prompt.append(originalPost)
        prompt.append("\n---\n\n")
        prompt.append("Provide ONLY the refined Bahasa Malaysia post, BUT ALWAYS use natural English football terminology where appropriate (e.g., 'Offside', 'Clean Sheet', 'Hat-trick'). ")
        prompt.append("Maintain the same length and structure. ")
        prompt.append("If there are bullet points, use • character only. ")
        prompt.append("Do NOT include any hashtags or explanations. ")
        prompt.append("Do NOT include any emojis in the output.\n")

        if (includeSource) {
            prompt.append(
                "\nEnsure the post ends with 'Sumber: [Source Name]' if the original post had one or if the source is known."
            )
        } else {
            prompt.append(
                "\nDo NOT include any 'Sumber:' citation in the output. Do NOT mention the source name, publication, or author anywhere in the post."
            )
        }

        return prompt.toString()
    }

    /**
     * Detects if the input text contains quotes.
     *
     * OPTIMIZATION: Uses a single-pass character scan to avoid multiple
     * linear scans of the string (O(N) vs O(6*N)).
     */
    fun containsQuotes(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false

        // Single pass O(N) instead of 6 calls to contains()
        for (i in 0 until text.length) {
            val c = text[i]
            if (c == '"' || c == '\u201C' || c == '\u201D' ||
                c == '\'' || c == '\u2018' || c == '\u2019') {
                return true
            }
        }
        return false
    }

    /**
     * Detects if content is long and technical (like The Athletic articles).
     * Criteria:
     * - Length > 2000 characters
     * - Contains tactical keywords
     * - Contains formation patterns or stat-heavy content
     *
     * OPTIMIZATION: Allocating a lowercase copy of the text once and using standard contains()
     * is ~4x faster than repeated case-insensitive scans for multiple keywords.
     * The memory cost of one string allocation is negligible compared to the CPU savings.
     */
    fun isLongTechnicalContent(text: String?): Boolean {
        if (text == null || text.length < 2000) return false

        // Convert to lowercase ONCE to enable fast indexOf scanning
        // Use Locale.ROOT to ensure consistent behavior across all user locales (avoiding Turkish-I issues)
        val lowerText = text.lowercase(java.util.Locale.ROOT)

        var keywordCount = 0
        for (keyword in TACTICAL_KEYWORDS) {
            // Keywords in TACTICAL_KEYWORDS must be lowercase
            if (lowerText.contains(keyword)) {
                keywordCount++
                // Early exit if we found enough keywords
                if (keywordCount >= 5) return true
            }
        }

        return false
    }

    private val TACTICAL_KEYWORDS = arrayOf(
        "formation", "tactical", "pressing", "possession", "xg", "expected goals",
        "pass completion", "progressive passes", "defensive line", "build-up",
        "counter-attack", "high press", "low block", "transition", "shape",
        "midfielder", "forward", "defender", "fullback", "winger",
        "4-3-3", "4-4-2", "3-5-2", "4-2-3-1", "5-3-2", "3-4-3"
    )

    /**
     * Detects if the input text contains bullet points or list markers.
     *
     * OPTIMIZATION: Uses a single-pass character scan to avoid allocating
     * a List<String> with lines(), creating substring objects with trim(),
     * and repeatedly compiling Regex patterns.
     */
    fun containsBulletPoints(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false

        val len = text.length
        var i = 0

        while (i < len) {
            // 1. Skip whitespace at start of line to find content
            var contentStart = i
            while (contentStart < len) {
                val c = text[contentStart]
                if (c != ' ' && c != '\t') break
                contentStart++
            }

            // Check if we reached end of string
            if (contentStart >= len) break

            val firstChar = text[contentStart]

            // If it's a newline, it's an empty line (or just whitespace), continue to next line
            if (firstChar == '\n' || firstChar == '\r') {
                // Logic below will advance 'i' to next line
            } else {
                // We are at the first non-whitespace character of a line

                // Check simple markers: • (U+2022), · (U+00B7)
                if (firstChar == '•' || firstChar == '·') return true

                // Check 2-char markers: "- ", "* "
                if (contentStart + 1 < len) {
                    val secondChar = text[contentStart + 1]
                    if ((firstChar == '-' || firstChar == '*') && secondChar == ' ') {
                        return true
                    }
                }

                // Check numbered lists: "1. ", "1) "
                if (firstChar in '0'..'9') {
                    var j = contentStart + 1
                    var digitCount = 1
                    while (j < len && text[j] in '0'..'9') {
                        j++
                        digitCount++
                    }
                    if (j < len) {
                        val afterDigits = text[j]
                        // Must be followed by dot or closing paren, AND then a space
                        // DEFENSIVE: Limit to 3 digits to avoid detecting years (e.g. "2024. ") as list items
                        if (digitCount <= 3 &&
                            (afterDigits == '.' || afterDigits == ')') &&
                            (j + 1 < len && text[j + 1] == ' ')) {
                            return true
                        }
                    }
                }

                // Check letter lists: "a) "
                if (firstChar in 'a'..'z') {
                    if (contentStart + 2 < len) {
                        if (text[contentStart + 1] == ')' && text[contentStart + 2] == ' ') {
                            return true
                        }
                    }
                }
            }

            // 2. Skip to next line
            while (i < len) {
                val c = text[i]
                if (c == '\n') {
                    i++
                    break
                }
                if (c == '\r') {
                    i++
                    if (i < len && text[i] == '\n') i++
                    break
                }
                i++
            }
        }

        return false
    }
}
