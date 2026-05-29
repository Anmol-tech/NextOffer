package com.example.nextoffer.resume;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class LatexSectionScanner {

    private static final Pattern SECTION_HEADER = Pattern.compile("\\\\section\\*?\\{([^}]+)\\}");
    private static final Pattern COMMENT_SECTION = Pattern.compile("(?i)%\\s*-+\\s*([A-Z0-9][A-Z0-9\\s/&\\-]+?)\\s*-+\\s*");

    private LatexSectionScanner() {
    }

    record Section(String title, List<String> lines) {
    }

    static List<Section> splitBody(String body) {
        List<String> lines = new ArrayList<>(List.of(body.split("\\R", -1)));
        List<Section> sections = new ArrayList<>();
        List<String> currentLines = new ArrayList<>();
        String currentTitle = "";

        for (String line : lines) {
            String markerTitle = detectSectionTitle(line);
            if (markerTitle != null) {
                if (!currentLines.isEmpty() || !currentTitle.isBlank() || sections.isEmpty()) {
                    sections.add(new Section(currentTitle, List.copyOf(currentLines)));
                }
                currentTitle = markerTitle;
                currentLines = new ArrayList<>();
                currentLines.add(line);
                continue;
            }
            currentLines.add(line);
        }

        if (!currentLines.isEmpty() || !currentTitle.isBlank() || sections.isEmpty()) {
            sections.add(new Section(currentTitle, List.copyOf(currentLines)));
        }

        return sections;
    }

    static boolean hasExplicitSections(String body) {
        for (String line : body.split("\\R")) {
            if (detectSectionTitle(line) != null) {
                return true;
            }
        }
        return false;
    }

    static SectionKind classify(String title) {
        if (title == null || title.isBlank()) {
            return SectionKind.PRELUDE;
        }
        String normalized = title.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("skill")) {
            return SectionKind.SKILLS;
        }
        if (normalized.contains("experience") || normalized.contains("employment")) {
            return SectionKind.EXPERIENCE;
        }
        if (normalized.contains("project")) {
            return SectionKind.PROJECTS;
        }
        if (normalized.contains("summary") || normalized.contains("profile") || normalized.contains("objective")) {
            return SectionKind.SUMMARY;
        }
        return SectionKind.OTHER;
    }

    static String sectionTitleFromLine(String line) {
        return detectSectionTitle(line);
    }

    private static String detectSectionTitle(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        Matcher section = SECTION_HEADER.matcher(trimmed);
        if (section.find()) {
            return section.group(1).trim();
        }
        Matcher comment = COMMENT_SECTION.matcher(trimmed);
        if (comment.find()) {
            return comment.group(1).trim();
        }
        return null;
    }

    enum SectionKind {
        PRELUDE,
        SUMMARY,
        SKILLS,
        EXPERIENCE,
        PROJECTS,
        OTHER
    }
}
