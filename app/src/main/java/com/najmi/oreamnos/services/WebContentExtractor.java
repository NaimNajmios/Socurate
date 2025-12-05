package com.mycompany.oreamnos.services;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Extracts main content from web URLs.
 * Replaces the CrawlerServlet functionality from the original web app.
 * Uses Jsoup for HTML parsing and OkHttp for fetching content.
 */
public class WebContentExtractor {

    private static final String TAG = "WebContentExtractor";
    private final OkHttpClient client;

    /**
     * Creates a new WebContentExtractor instance.
     */
    public WebContentExtractor() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();
    }

    /**
     * Extracts the main content from a URL.
     * 
     * @param url The URL to extract content from
     * @return The extracted text content
     * @throws Exception if extraction fails
     */
    public String extractContent(String url) throws Exception {
        if (url == null || url.trim().isEmpty()) {
            throw new Exception("URL cannot be empty");
        }

        // Validate URL format
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        Log.i(TAG, "Fetching content from: " + url);

        // Fetch HTML content
        String html = fetchHtml(url);

        // Parse and extract main content
        String content = parseContent(html, url);

        if (content == null || content.trim().isEmpty()) {
            throw new Exception("Could not extract meaningful content from URL");
        }

        Log.i(TAG, "Extracted " + content.length() + " characters from URL");
        return content;
    }

    /**
     * Fetches raw HTML from the URL.
     */
    private String fetchHtml(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent",
                        "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch URL: HTTP " + response.code());
            }

            if (response.body() == null) {
                throw new IOException("Response body is empty");
            }

            return response.body().string();
        }
    }

    /**
     * Parses HTML and extracts main content.
     * Tries multiple strategies to find the article content.
     */
    private String parseContent(String html, String url) {
        Document doc = Jsoup.parse(html);

        // Remove unwanted elements
        removeUnwantedElements(doc);

        // Try different content extraction strategies
        String content = null;

        // Strategy 1: Look for article tag
        Elements articles = doc.select("article");
        if (!articles.isEmpty()) {
            content = extractTextFromElements(articles);
        }

        // Strategy 2: Look for main content div
        if (content == null || content.length() < 200) {
            Elements mainContent = doc
                    .select("div[role=main], main, #main-content, .article-body, .post-content, .entry-content");
            if (!mainContent.isEmpty()) {
                content = extractTextFromElements(mainContent);
            }
        }

        // Strategy 3: Look for meta description and combine with paragraphs
        if (content == null || content.length() < 200) {
            StringBuilder sb = new StringBuilder();

            // Get meta description
            Element metaDesc = doc.selectFirst("meta[name=description], meta[property=og:description]");
            if (metaDesc != null) {
                String desc = metaDesc.attr("content");
                if (desc != null && !desc.isEmpty()) {
                    sb.append(desc).append("\n\n");
                }
            }

            // Get all paragraphs
            Elements paragraphs = doc.select("p");
            for (Element p : paragraphs) {
                String text = p.text().trim();
                // Only include substantial paragraphs
                if (text.length() > 50) {
                    sb.append(text).append("\n\n");
                }
            }

            content = sb.toString().trim();
        }

        // Strategy 4: Fallback to body text
        if (content == null || content.length() < 100) {
            content = doc.body().text();
        }

        // Clean up the content
        content = cleanContent(content);

        return content;
    }

    /**
     * Removes unwanted elements from the document.
     */
    private void removeUnwantedElements(Document doc) {
        // Remove scripts, styles, navigation, ads, etc.
        doc.select(
                "script, style, nav, header, footer, aside, .advertisement, .ad, .social-share, .comments, #comments, .related-posts")
                .remove();
    }

    /**
     * Extracts text from a collection of elements.
     */
    private String extractTextFromElements(Elements elements) {
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            String text = element.text().trim();
            if (!text.isEmpty()) {
                sb.append(text).append("\n\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Cleans up extracted content.
     */
    private String cleanContent(String content) {
        if (content == null) {
            return "";
        }

        // Remove excessive whitespace
        content = content.replaceAll("\\s+", " ");

        // Remove excessive newlines
        content = content.replaceAll("(?:\\n\\s*){3,}", "\n\n");

        // Remove common boilerplate phrases
        content = content.replaceAll("(?i)\\bclick here\\b.*?\\bmore\\b", "");
        content = content.replaceAll("(?i)\\bshare this\\b.*?\\bfacebook\\b", "");
        content = content.replaceAll("(?i)\\bsubscribe.*?newsletter\\b", "");

        return content.trim();
    }

    /**
     * Checks if a string looks like a URL.
     */
    public static boolean isUrl(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lower = text.trim().toLowerCase();
        return lower.startsWith("http://") ||
                lower.startsWith("https://") ||
                lower.startsWith("www.") ||
                (lower.contains(".") && !lower.contains(" ") && lower.length() > 5);
    }
}
