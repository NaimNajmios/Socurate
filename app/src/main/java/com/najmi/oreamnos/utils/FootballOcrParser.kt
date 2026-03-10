package com.najmi.oreamnos.utils

/**
 * Parser for football-specific OCR text.
 * Improves extraction quality by identifying common patterns in sports stats.
 */
object FootballOcrParser {

    /**
     * Formats the raw OCR text for the AI prompt.
     * Detects stat lines and matchday details.
     */
    fun formatForPrompt(rawText: String): String {
        if (rawText.isBlank()) return ""

        val lines = rawText.lines()
        val processedLines = mutableListOf<String>()
        
        processedLines.add("[Extracted from screenshot]")
        processedLines.add("---")

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            // Detect scoreline (e.g. "Team A 2-1 Team B" or "Man City 3 - 0 Arsenal")
            if (isScoreline(trimmedLine)) {
                processedLines.add("MATCH RESULT: $trimmedLine")
                continue
            }

            // Detect stat lines (e.g. "Goals: 2", "Assists: 1", "Rating: 8.5")
            val statLabel = detectStatLabel(trimmedLine)
            if (statLabel != null) {
                processedLines.add("$statLabel: ${extractNumericalValue(trimmedLine)}")
                continue
            }

            processedLines.add(trimmedLine)
        }

        return processedLines.joinToString("\n")
    }

    private fun isScoreline(line: String): Boolean {
        // Pattern: Team Names followed by digit-digit or digit - digit
        return line.contains(Regex("\\d\\s*[-]\\s*\\d"))
    }

    private fun detectStatLabel(line: String): String? {
        val lowerLine = line.lowercase()
        return when {
            lowerLine.contains("goal") -> "GOALS"
            lowerLine.contains("assist") -> "ASSISTS"
            lowerLine.contains("rating") -> "RATING"
            lowerLine.contains("pass") && lowerLine.contains("%") -> "PASS ACCURACY"
            lowerLine.contains("tackle") -> "TACKLES"
            lowerLine.contains("save") -> "SAVES"
            lowerLine.contains("yellow") -> "YELLOW CARDS"
            lowerLine.contains("red") -> "RED CARDS"
            else -> null
        }
    }

    private fun extractNumericalValue(line: String): String {
        // Try to find the first number in the line
        val match = Regex("\\d+(\\.\\d+)?").find(line)
        return match?.value ?: line
    }
}
