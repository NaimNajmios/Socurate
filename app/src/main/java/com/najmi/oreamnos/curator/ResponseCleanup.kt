package com.najmi.oreamnos.curator

import java.util.regex.Pattern

object ResponseCleanup {

    val horizontalRulePattern: Pattern = Pattern.compile("(?m)^-{3,}\\s*$")
    val multipleNewlinesPattern: Pattern = Pattern.compile("\\n\\s*\\n\\s*\\n+")
    val horizontalWhitespacePattern: Pattern = Pattern.compile("[ \\t]+")
    val sourceCitationPattern: Pattern = Pattern.compile("(?im)^[\\s\\p{Z}]*[*_]*(?:Sumber|Source)[*_]*[\\s\\p{Z}]*[:：].*$")
    val trailingNewlinesPattern: Pattern = Pattern.compile("\\n+$")
    val bulletPointPattern: Pattern = Pattern.compile("(?m)^(\\s*)[-*>\u2022\u25e6\u25aa\u25ab\u2023\u2043](\\s+)")
    val asteriskTextPattern: Pattern = Pattern.compile("\\*+(.*?)\\*+")

    private val unwantedPhrases = arrayOf(
        "Okay, ini percubaan untuk mengubah teks tersebut",
        "terjemahkan ke Bahasa Melayu (Malaysia)",
        "suntikkan sedikit gaya yang kurang formal",
        "istilah bola sepak Inggeris yang biasa",
        "Saya cuba gunakan perkataan yang lebih santai",
        "Saya juga masukkan istilah bola sepakt",
        "Struktur diubah dengan menggabungkan",
        "Em dash (—) dibuang seperti yang diminta",
        "Tukar perkataan dari bahasa inggeris",
        "Semoga ini membantu",
        "Saya cuba",
        "Saya juga",
        "Struktur diubah",
        "Em dash",
        "Tukar perkataan",
        "Semoga ini"
    )

    fun removeSourceCitation(text: String?): String {
        if (text.isNullOrEmpty()) return text ?: ""

        val cleaned = sourceCitationPattern.matcher(text).replaceAll("")
        return trailingNewlinesPattern.matcher(cleaned).replaceAll("").trim()
    }

    fun cleanUpResponse(response: String?, includeSource: Boolean = true): String {
        if (response.isNullOrBlank()) return response ?: ""

        var cleaned = response.trim()

        cleaned = horizontalRulePattern.matcher(cleaned).replaceAll("")

        for (phrase in unwantedPhrases) {
            cleaned = cleaned.replace(phrase, "")
        }

        cleaned = bulletPointPattern.matcher(cleaned).replaceAll("$1•$2")

        cleaned = multipleNewlinesPattern.matcher(cleaned).replaceAll("\n\n")
        cleaned = horizontalWhitespacePattern.matcher(cleaned).replaceAll(" ")
        cleaned = cleaned.trim()

        if (cleaned.length < 50) {
            return response
        }

        return cleaned
    }

    fun cleanUpResponseWithMarkdown(response: String?, includeSource: Boolean = true): String {
        var cleaned = cleanUpResponse(response, includeSource)
        cleaned = asteriskTextPattern.matcher(cleaned).replaceAll("$1")
        return cleaned
    }
}
