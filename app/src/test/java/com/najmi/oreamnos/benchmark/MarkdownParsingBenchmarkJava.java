package com.najmi.oreamnos.benchmark;

/**
 * Benchmark test for MarkdownUtils optimization.
 * This test verifies the performance improvement of using a single-pass scan
 * for inline formatting markers compared to repeated indexOf calls.
 *
 * NOTE: This is a standalone benchmark file. In a real environment,
 * this would be part of the test suite or microbenchmark library.
 */
public class MarkdownParsingBenchmarkJava {

    // --- Original Logic (Simulated) ---

    static void parseOriginal(String text, int start, int end, StringBuilder out) {
        int currentIndex = start;

        while (currentIndex < end) {
            // Look for bold (**text**)
            int boldStart = indexOf(text, "**", currentIndex, end);

            // Look for italic (*text* or _text_)
            int italicStarStart = indexOf(text, "*", currentIndex, end);
            if (italicStarStart != -1 && italicStarStart + 1 < end && text.charAt(italicStarStart + 1) == '*') {
                italicStarStart = -1;
            }

            int italicUnderStart = indexOf(text, "_", currentIndex, end);
            if (italicUnderStart != -1 && italicUnderStart + 1 < end && text.charAt(italicUnderStart + 1) == '_') {
                italicUnderStart = -1;
            }

            int formatStart = -1;
            String formatType = "";

            if (boldStart != -1) {
                formatStart = boldStart;
                formatType = "bold";
            }

            if (italicStarStart != -1) {
                if (formatStart == -1 || italicStarStart < formatStart) {
                    formatStart = italicStarStart;
                    formatType = "italic_star";
                }
            }

            if (italicUnderStart != -1) {
                if (formatStart == -1 || italicUnderStart < formatStart) {
                    formatStart = italicUnderStart;
                    formatType = "italic_under";
                }
            }

            if (formatStart == -1) {
                out.append("RAW[").append(text.substring(currentIndex, end)).append("]");
                break;
            }

            out.append("RAW[").append(text.substring(currentIndex, formatStart)).append("]");

            if (formatType.equals("bold")) {
                int boldEnd = indexOf(text, "**", formatStart + 2, end);
                if (boldEnd != -1) {
                    out.append("BOLD[").append(text.substring(formatStart + 2, boldEnd)).append("]");
                    currentIndex = boldEnd + 2;
                } else {
                    out.append("RAW[**]");
                    currentIndex = formatStart + 2;
                }
            } else if (formatType.equals("italic_star")) {
                int italicEnd = indexOf(text, "*", formatStart + 1, end);
                if (italicEnd != -1) {
                    out.append("ITALIC[").append(text.substring(formatStart + 1, italicEnd)).append("]");
                    currentIndex = italicEnd + 1;
                } else {
                    out.append("RAW[*]");
                    currentIndex = formatStart + 1;
                }
            } else if (formatType.equals("italic_under")) {
                int italicEnd = indexOf(text, "_", formatStart + 1, end);
                if (italicEnd != -1) {
                    out.append("ITALIC[").append(text.substring(formatStart + 1, italicEnd)).append("]");
                    currentIndex = italicEnd + 1;
                } else {
                    out.append("RAW[_]");
                    currentIndex = formatStart + 1;
                }
            }
        }
    }

    static int indexOf(String text, String needle, int start, int end) {
        if (start >= end) return -1;
        int idx = text.indexOf(needle, start);
        return (idx != -1 && idx < end) ? idx : -1;
    }

    // --- Optimized Logic ---

    static void parseOptimized(String text, int start, int end, StringBuilder out) {
        int currentIndex = start;

        while (currentIndex < end) {
            int nextMarker = -1;
            String formatType = "";

            // Single pass scan to find the earliest marker
            for (int i = currentIndex; i < end; i++) {
                char c = text.charAt(i);
                if (c == '*') {
                    if (i + 1 < end && text.charAt(i + 1) == '*') {
                        nextMarker = i;
                        formatType = "bold";
                        break;
                    } else {
                        nextMarker = i;
                        formatType = "italic_star";
                        break;
                    }
                } else if (c == '_') {
                     if (i + 1 < end && text.charAt(i + 1) == '_') {
                        // Double underscore, skip this char and the next
                        i++;
                        continue;
                    } else {
                        nextMarker = i;
                        formatType = "italic_under";
                        break;
                    }
                }
            }

            if (nextMarker == -1) {
                out.append("RAW[").append(text.substring(currentIndex, end)).append("]");
                break;
            }

            out.append("RAW[").append(text.substring(currentIndex, nextMarker)).append("]");

            if (formatType.equals("bold")) {
                int boldEnd = indexOf(text, "**", nextMarker + 2, end);
                if (boldEnd != -1) {
                    out.append("BOLD[").append(text.substring(nextMarker + 2, boldEnd)).append("]");
                    currentIndex = boldEnd + 2;
                } else {
                    out.append("RAW[**]");
                    currentIndex = nextMarker + 2;
                }
            } else if (formatType.equals("italic_star")) {
                int italicEnd = indexOf(text, "*", nextMarker + 1, end);
                if (italicEnd != -1) {
                    out.append("ITALIC[").append(text.substring(nextMarker + 1, italicEnd)).append("]");
                    currentIndex = italicEnd + 1;
                } else {
                    out.append("RAW[*]");
                    currentIndex = nextMarker + 1;
                }
            } else if (formatType.equals("italic_under")) {
                int italicEnd = indexOf(text, "_", nextMarker + 1, end);
                if (italicEnd != -1) {
                    out.append("ITALIC[").append(text.substring(nextMarker + 1, italicEnd)).append("]");
                    currentIndex = italicEnd + 1;
                } else {
                    out.append("RAW[_]");
                    currentIndex = nextMarker + 1;
                }
            }
        }
    }

    public static void main(String[] args) {
        // --- Logic Verification ---
        System.out.println("Verifying logic...");
        String[] testCases = {
            "**bold**",
            "*italic*",
            "_italic_",
            "**bold** and *italic*",
            "normal text",
            "unclosed **bold",
            "mixed **bold** and _italic_ and *star*",
            "nested **bold *italic* bold**",
            "escaped __double underscore__",
            "**bold** with *italic* inside? No, flat.",
            "***bolditalic***",
            "__literal__",
            "**one** *two* _three_ **four**",
            "____",
            "_*_",
            "**"
        };

        for (String testCase : testCases) {
            StringBuilder outOrig = new StringBuilder();
            parseOriginal(testCase, 0, testCase.length(), outOrig);

            StringBuilder outOpt = new StringBuilder();
            parseOptimized(testCase, 0, testCase.length(), outOpt);

            if (!outOrig.toString().equals(outOpt.toString())) {
                System.err.println("MISMATCH for: " + testCase);
                System.err.println("Orig: " + outOrig);
                System.err.println("Opt : " + outOpt);
                throw new RuntimeException("Logic Mismatch");
            }
        }
        System.out.println("Logic verification passed.");

        // --- Benchmarking ---
        StringBuilder sb = new StringBuilder();
        // Generate a long text with sparse formatting
        for (int i = 0; i < 10000; i++) {
            sb.append("This is some normal text that is quite long and boring. ");
            // Much sparser formatting
            if (i % 2000 == 0) sb.append("**bold** ");
        }
        String text = sb.toString();
        int len = text.length();

        System.out.println("Text length: " + len);

        StringBuilder dummy = new StringBuilder();

        // Warmup
        for (int i = 0; i < 100; i++) {
            dummy.setLength(0);
            parseOriginal(text, 0, len, dummy);
            dummy.setLength(0);
            parseOptimized(text, 0, len, dummy);
        }

        long start = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            dummy.setLength(0);
            parseOriginal(text, 0, len, dummy);
        }
        long end = System.nanoTime();
        double originalTime = (end - start) / 1_000_000.0;

        start = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            dummy.setLength(0);
            parseOptimized(text, 0, len, dummy);
        }
        end = System.nanoTime();
        double optimizedTime = (end - start) / 1_000_000.0;

        System.out.println("Original: " + originalTime + " ms");
        System.out.println("Optimized: " + optimizedTime + " ms");
        System.out.println("Speedup: " + (originalTime / optimizedTime) + "x");
    }
}
