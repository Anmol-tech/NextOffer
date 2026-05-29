package com.example.nextoffer.resume;

public enum ResumeSourceFormat {
    TEXT,
    LATEX;

    public static ResumeSourceFormat detect(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return TEXT;
        }
        String sample = rawText.trim();
        if (sample.contains("\\documentclass")
                || sample.contains("\\begin{document}")
                || (sample.contains("\\section") && sample.contains("\\item"))) {
            return LATEX;
        }
        return TEXT;
    }
}
