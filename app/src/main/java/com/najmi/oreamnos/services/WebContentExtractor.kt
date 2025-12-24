package com.najmi.oreamnos.services

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Extracts main content from web URLs.
 * Replaces the CrawlerServlet functionality from the original web app.
 * Uses Jsoup for HTML parsing and OkHttp for fetching content.
 */
class WebContentExtractor {

    /**
     * Extracts the main content from a URL.
     */
    @Throws(Exception::class)
    fun extractContent(url: String?): String {
        var processedUrl = url
        if (processedUrl.isNullOrBlank()) {
            throw Exception("URL cannot be empty")
        }

        // Validate URL format
        if (!processedUrl.startsWith("http://") && !processedUrl.startsWith("https://")) {
            processedUrl = "https://$processedUrl"
        }

        Log.i(TAG, "Fetching content from: $processedUrl")

        // Fetch HTML content
        val html = fetchHtml(processedUrl)

        // Parse and extract main content
        val content = parseContent(html, processedUrl)

        if (content.isNullOrBlank()) {
            throw Exception("Could not extract meaningful content from URL")
        }

        Log.i(TAG, "Extracted ${content.length} characters from URL")
        return content
    }

    /**
     * Fetches raw HTML from the URL.
     */
    @Throws(IOException::class)
    private fun fetchHtml(url: String): String {
        val request = Request.Builder()
            .url(url)
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
            )
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to fetch URL: HTTP ${response.code}")
            }

            val body = response.body ?: throw IOException("Response body is empty")
            return body.string()
        }
    }

    /**
     * Parses HTML and extracts main content.
     */
    private fun parseContent(html: String, url: String): String? {
        val doc = Jsoup.parse(html)

        // Remove unwanted elements
        removeUnwantedElements(doc)

        var content: String? = null

        // Strategy 1: Look for article tag
        val articles = doc.select("article")
        if (articles.isNotEmpty()) {
            content = extractTextFromElements(articles)
        }

        // Strategy 2: Look for main content div
        if (content.isNullOrEmpty() || content.length < 200) {
            val mainContent = doc.select(
                "div[role=main], main, #main-content, .article-body, .post-content, .entry-content"
            )
            if (mainContent.isNotEmpty()) {
                content = extractTextFromElements(mainContent)
            }
        }

        // Strategy 3: Look for meta description and combine with paragraphs
        if (content.isNullOrEmpty() || content.length < 200) {
            val sb = StringBuilder()

            // Get meta description
            val metaDesc = doc.selectFirst("meta[name=description], meta[property=og:description]")
            metaDesc?.attr("content")?.takeIf { it.isNotEmpty() }?.let {
                sb.append(it).append("\n\n")
            }

            // Get all paragraphs
            doc.select("p").forEach { p ->
                val text = p.text().trim()
                if (text.length > 50) {
                    sb.append(text).append("\n\n")
                }
            }

            content = sb.toString().trim()
        }

        // Strategy 4: Fallback to body text
        if (content.isNullOrEmpty() || content.length < 100) {
            content = doc.body().text()
        }

        return cleanContent(content)
    }

    /**
     * Removes unwanted elements from the document.
     */
    private fun removeUnwantedElements(doc: Document) {
        doc.select(
            "script, style, nav, header, footer, aside, .advertisement, .ad, .social-share, .comments, #comments, .related-posts"
        ).remove()
    }

    /**
     * Extracts text from a collection of elements.
     */
    private fun extractTextFromElements(elements: Elements): String {
        return elements.mapNotNull { element ->
            element.text().trim().takeIf { it.isNotEmpty() }
        }.joinToString("\n\n")
    }

    /**
     * Cleans up extracted content.
     */
    private fun cleanContent(content: String?): String {
        if (content == null) return ""

        var cleaned = content
        cleaned = WHITESPACE_PATTERN.matcher(cleaned).replaceAll(" ")
        cleaned = NEWLINES_PATTERN.matcher(cleaned).replaceAll("\n\n")
        cleaned = CLICK_HERE_PATTERN.matcher(cleaned).replaceAll("")
        cleaned = SHARE_THIS_PATTERN.matcher(cleaned).replaceAll("")
        cleaned = SUBSCRIBE_PATTERN.matcher(cleaned).replaceAll("")

        return cleaned.trim()
    }

    /**
     * Extracts metadata (title, favicon, domain) from a URL.
     */
    @Throws(Exception::class)
    fun extractMetadata(url: String?): UrlMetadata {
        var processedUrl = url
        if (processedUrl.isNullOrBlank()) {
            throw Exception("URL cannot be empty")
        }

        if (!processedUrl.startsWith("http://") && !processedUrl.startsWith("https://")) {
            processedUrl = "https://$processedUrl"
        }

        val html = fetchHtml(processedUrl)
        val doc = Jsoup.parse(html)

        // Extract Title
        var title = doc.selectFirst("meta[property=og:title]")?.attr("content")
        if (title.isNullOrEmpty()) {
            title = doc.title()
        }

        // Extract Favicon
        var faviconUrl: String? = doc.selectFirst("link[rel=apple-touch-icon]")?.attr("href")
        if (faviconUrl == null) {
            faviconUrl = doc.selectFirst("link[rel~=icon]")?.attr("href")
        }
        if (faviconUrl == null) {
            faviconUrl = doc.selectFirst("meta[property=og:image]")?.attr("content")
        }

        // Resolve relative URLs for favicon
        if (faviconUrl != null && !faviconUrl.startsWith("http")) {
            try {
                val baseUrl = URL(processedUrl)
                val absoluteUrl = URL(baseUrl, faviconUrl)
                faviconUrl = absoluteUrl.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Error resolving favicon URL: ${e.message}")
            }
        }

        // Extract Domain
        val domain = try {
            val netUrl = URL(processedUrl)
            netUrl.host.removePrefix("www.")
        } catch (e: Exception) {
            processedUrl
        }

        return UrlMetadata(title, faviconUrl, domain, processedUrl)
    }

    /**
     * Data class for URL metadata.
     */
    data class UrlMetadata(
        @JvmField val title: String?,
        @JvmField val faviconUrl: String?,
        @JvmField val domain: String,
        @JvmField val originalUrl: String
    )

    companion object {
        private const val TAG = "WebContentExtractor"

        // Share OkHttpClient instance to reuse connection pool and threads
        private val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()

        // Pre-compiled regex patterns for better performance
        private val WHITESPACE_PATTERN: Pattern = Pattern.compile("\\s+")
        private val NEWLINES_PATTERN: Pattern = Pattern.compile("(?:\\n\\s*){3,}")
        private val CLICK_HERE_PATTERN: Pattern = Pattern.compile("(?i)\\bclick here\\b.*?\\bmore\\b")
        private val SHARE_THIS_PATTERN: Pattern = Pattern.compile("(?i)\\bshare this\\b.*?\\bfacebook\\b")
        private val SUBSCRIBE_PATTERN: Pattern = Pattern.compile("(?i)\\bsubscribe.*?newsletter\\b")

        /**
         * Checks if a string looks like a URL.
         */
        @JvmStatic
        fun isUrl(text: String?): Boolean {
            if (text.isNullOrBlank()) return false

            val lower = text.trim().lowercase()
            return lower.startsWith("http://") ||
                    lower.startsWith("https://") ||
                    lower.startsWith("www.") ||
                    (lower.contains(".") && !lower.contains(" ") && lower.length > 5)
        }
    }
}
