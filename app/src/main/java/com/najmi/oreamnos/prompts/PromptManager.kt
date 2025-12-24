package com.najmi.oreamnos.prompts

/**
 * Centralizes prompt engineering for content curation.
 * Extracted from GeminiService for reusability across different AI providers.
 */
class PromptManager {

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
    fun buildInitialPrompt(tone: String, inputText: String, includeSource: Boolean, keepStructure: Boolean): String {
        val originalLength = inputText.length
        var targetMinLength = (originalLength * 0.4).toInt()
        var targetMaxLength = (originalLength * 0.6).toInt()

        // Ensure reasonable defaults if text is short
        if (targetMinLength < 50) targetMinLength = 50
        if (targetMaxLength < 100) targetMaxLength = 100

        val toneDesc = if (tone == "formal") "formal, professional" else "engaging, conversational"
        val toneInstruction = if (tone == "formal")
            "Maintain a formal, professional tone suitable for official club communication"
        else
            "Maintain an engaging, conversational tone suitable for fan communities"

        // Detect quotes in input
        val hasQuotes = containsQuotes(inputText)

        // Detect long/technical content
        val isTechnicalArticle = isLongTechnicalContent(inputText)

        // Build base prompt
        val prompt = StringBuilder()
        prompt.append(
            "You are a professional social media content writer for a Malaysian football club. Your task is to transform the following English football news article into a "
        ).append(toneDesc).append(" social media post written in Malaysian Malay (Bahasa Malaysia).\n\n")

        prompt.append("STRICT REQUIREMENTS:\n")
            .append("1. Write ONLY in Bahasa Malaysia (Malaysian Malay) - do not include any English text in your output\n")
            .append("2. ").append(toneInstruction).append("\n")

        if (keepStructure) {
            prompt.append(
                "3. STRICTLY PRESERVE the original formatting, bullet points, lists, and structure. Do NOT summarize into paragraphs if the original used a list format. Translate the content line-by-line while keeping the visual layout exactly the same.\n"
            )
        } else {
            prompt.append("3. The output must be approximately 40-60% of the original content length (target: ")
                .append(targetMinLength).append("-").append(targetMaxLength).append(" characters)\n")
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
            ". Start the Title with a HIGHLY context-specific and diverse emoji (e.g., 🏥, 🌬️, 💰, 🚨). ALSO start the FIRST paragraph of the body with a context-specific emoji. Do NOT use emojis elsewhere.\n"
        )
        prompt.append(nextNum + 3)
            .append(". The tone should be that of an official club announcement or news update\n\n")

        prompt.append("ORIGINAL ENGLISH TEXT:\n---\n")
            .append(inputText).append("\n---\n\n")

        when {
            keepStructure -> prompt.append(
                "Provide ONLY the Bahasa Malaysia social media post. STRICTLY PRESERVE the original formatting (lists, bullets, spacing). Do NOT include any hashtags."
            )
            isTechnicalArticle -> prompt.append(
                "Provide ONLY the Bahasa Malaysia social media post. Structure it with a headline followed by Key Stats, Formations, and Tactical Shifts sections. Separate sections with blank lines. Do NOT include any hashtags."
            )
            else -> prompt.append(
                "Provide ONLY the Bahasa Malaysia social media post. Ensure the output is structured with a headline and paragraphs separated by blank lines. Do NOT include any hashtags."
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
                "formal" -> prompt.append(
                    "- Make it more Formal: Use formal language suitable for official club communication\n"
                )
                "conversational" -> prompt.append(
                    "- Make it more Conversational: Use engaging, conversational tone suitable for fan communities\n"
                )
                "shorten_detailed" -> prompt.append(
                    "- Shorten But Detailed: Make the post more concise while retaining all important details, facts, and key information. Remove redundant or filler words but keep the substance.\n"
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
        prompt.append("Provide ONLY the refined Bahasa Malaysia post. ")
        prompt.append("Maintain the same length and structure. ")
        prompt.append("Do NOT include any hashtags or explanations. ")
        prompt.append(
            "CRITICAL: You MUST include a context-relevant emoji at the start of the Title AND at the start of the First Paragraph of the body. Even if it seems redundant, you MUST do this.\n"
        )

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
     */
    fun containsQuotes(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        // Check for various quote marks
        return text.contains("\"") || text.contains("\u201C") || text.contains("\u201D") ||
                text.contains("'") || text.contains("\u2018") || text.contains("\u2019")
    }

    /**
     * Detects if content is long and technical (like The Athletic articles).
     * Criteria:
     * - Length > 2000 characters
     * - Contains tactical keywords
     * - Contains formation patterns or stat-heavy content
     */
    fun isLongTechnicalContent(text: String?): Boolean {
        if (text == null || text.length < 2000) return false

        // Convert to lowercase for keyword matching
        val lowerText = text.lowercase()

        // Count tactical keywords
        val tacticalKeywords = arrayOf(
            "formation", "tactical", "pressing", "possession", "xg", "expected goals",
            "pass completion", "progressive passes", "defensive line", "build-up",
            "counter-attack", "high press", "low block", "transition", "shape",
            "midfielder", "forward", "defender", "fullback", "winger",
            "4-3-3", "4-4-2", "3-5-2", "4-2-3-1", "5-3-2", "3-4-3"
        )

        var keywordCount = 0
        for (keyword in tacticalKeywords) {
            if (lowerText.contains(keyword)) {
                keywordCount++
            }
        }

        // If it has 5+ tactical keywords and is long, it's technical
        return keywordCount >= 5
    }
}
