package com.example.nextoffer.resume;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class LatexSanitizer {

    private static final Pattern RESUME_CLASS = Pattern.compile("\\\\documentclass\\s*(?:\\[[^\\]]*\\])?\\s*\\{resume\\}");
    private static final Pattern R_SECTION = Pattern.compile("\\\\begin\\{rSection\\}");

    public String sanitize(String latex) {
        if (latex == null || latex.isBlank()) {
            throw new ResumeGenerationException("Generated LaTeX is empty");
        }

        String normalized = latex.replace("\r\n", "\n").strip();
        normalized = ensureDocumentWrapper(normalized);
        normalized = removeOrphanProse(normalized);
        normalized = trimTrailingWhitespace(normalized);
        validateStructure(normalized);
        return normalized + "\n";
    }

    private static String ensureDocumentWrapper(String latex) {
        if (latex.contains("\\begin{document}") && latex.contains("\\end{document}")) {
            return latex;
        }
        throw new ResumeGenerationException("LaTeX must contain \\begin{document} and \\end{document}");
    }

    private static String removeOrphanProse(String latex) {
        if (!usesResumeSectionFormat(latex)) {
            return latex;
        }

        int docBegin = latex.indexOf("\\begin{document}");
        if (docBegin < 0) {
            return latex;
        }

        int bodyStart = docBegin + "\\begin{document}".length();
        int docEnd = latex.indexOf("\\end{document}", bodyStart);
        String preamble = latex.substring(0, bodyStart);
        String body = docEnd > bodyStart ? latex.substring(bodyStart, docEnd) : latex.substring(bodyStart);
        String postamble = docEnd > bodyStart ? latex.substring(docEnd) : "";

        List<String> lines = List.of(body.split("\n", -1));
        List<String> cleaned = new ArrayList<>();
        boolean reachedStructure = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (!reachedStructure) {
                if (trimmed.isBlank() || trimmed.startsWith("%")) {
                    cleaned.add(line);
                    continue;
                }
                if (isStructuralLine(trimmed)) {
                    reachedStructure = true;
                    cleaned.add(line);
                    continue;
                }
                // Drop orphan prose inserted before the first structural block.
                continue;
            }
            cleaned.add(line);
        }

        return preamble + String.join("\n", cleaned) + postamble;
    }

    private static boolean usesResumeSectionFormat(String latex) {
        return RESUME_CLASS.matcher(latex).find() || R_SECTION.matcher(latex).find();
    }

    private static boolean isStructuralLine(String trimmed) {
        if (trimmed.startsWith("\\")) {
            return true;
        }
        return trimmed.startsWith("%---") || trimmed.startsWith("%----------------");
    }

    private static void validateStructure(String latex) {
        assertBalanced(latex, "document");
        assertBalanced(latex, "rSection");
        assertBalanced(latex, "itemize");
        assertBalanced(latex, "enumerate");
        assertBalanced(latex, "center");
    }

    private static void assertBalanced(String latex, String environment) {
        String begin = "\\begin{" + environment + "}";
        String end = "\\end{" + environment + "}";
        int opens = countOccurrences(latex, begin);
        int closes = countOccurrences(latex, end);
        if (opens != closes) {
            throw new ResumeGenerationException(
                    "Unbalanced LaTeX environment '" + environment + "': "
                            + opens + " begin, " + closes + " end");
        }
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private static String trimTrailingWhitespace(String latex) {
        return latex.stripTrailing();
    }
}
