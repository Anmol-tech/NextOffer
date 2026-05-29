package com.example.nextoffer.resume;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LatexResumeParser {

    private static final Pattern ITEM_PREFIX = Pattern.compile("^\\s*\\\\item(?:\\[[^\\]]*\\])?\\s*(.*)$");

    public TailoredResumeContent parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new TailoredResumeContent("", List.of(), List.of());
        }

        String body = extractDocumentBody(rawText);
        if (!LatexSectionScanner.hasExplicitSections(body)) {
            return parseLegacyBody(body);
        }

        StringBuilder summary = new StringBuilder();
        List<String> skills = new ArrayList<>();
        List<String> bullets = new ArrayList<>();

        for (LatexSectionScanner.Section section : LatexSectionScanner.splitBody(body)) {
            switch (LatexSectionScanner.classify(section.title())) {
                case PRELUDE, SUMMARY -> appendSummaryText(summary, section.lines());
                case SKILLS -> skills.addAll(extractSkills(section.lines()));
                case EXPERIENCE -> bullets.addAll(extractItemTexts(section.lines()));
                case PROJECTS, OTHER -> {
                }
            }
        }

        return buildContent(summary.toString(), skills, bullets);
    }

    private TailoredResumeContent parseLegacyBody(String body) {
        List<String> lines = body.lines().toList();
        String summary = lines.isEmpty() ? "" : lines.getFirst();
        List<String> skills = new ArrayList<>();
        List<String> bullets = new ArrayList<>();
        boolean inSkills = false;

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            String lower = line.toLowerCase(Locale.ROOT);

            if (lower.startsWith("skills:") || lower.equals("skills")) {
                inSkills = true;
                String inline = line.contains(":") ? line.substring(line.indexOf(':') + 1).trim() : "";
                if (!inline.isBlank()) {
                    skills.addAll(splitInlineList(inline));
                }
                continue;
            }

            if (lower.startsWith("experience:") || lower.equals("experience")) {
                inSkills = false;
                continue;
            }

            if (line.startsWith("-") || line.startsWith("*") || line.startsWith("•")) {
                inSkills = false;
                bullets.add(line.replaceFirst("^[-*•]\\s+", "").trim());
                continue;
            }

            if (inSkills) {
                skills.addAll(splitInlineList(line));
            } else if (line.length() > 20) {
                bullets.add(line);
            }
        }

        if (bullets.isEmpty() && lines.size() > 1) {
            bullets.addAll(lines.subList(1, Math.min(lines.size(), 6)));
        }

        return buildContent(summary, skills, bullets);
    }

    private void appendSummaryText(StringBuilder summary, List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isBlank()
                    || trimmed.startsWith("%")
                    || trimmed.startsWith("\\begin{")
                    || trimmed.startsWith("\\end{")
                    || trimmed.startsWith("\\item")
                    || isHeaderLine(trimmed)) {
                continue;
            }
            String text = stripLatex(trimmed);
            if (!text.isBlank()) {
                if (!summary.isEmpty()) {
                    summary.append(' ');
                }
                summary.append(text);
            }
        }
    }

    private List<String> extractSkills(List<String> lines) {
        List<String> skills = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("\\item")) {
                skills.add(stripLatex(trimmed.replaceFirst("^\\\\item(?:\\[[^\\]]*\\])?\\s*", "")));
            } else if (!trimmed.startsWith("%") && trimmed.contains(":") && trimmed.startsWith("\\textbf{")) {
                skills.addAll(splitInlineList(stripLatex(trimmed)));
            } else if (!trimmed.startsWith("%") && !trimmed.startsWith("\\")) {
                skills.addAll(splitInlineList(stripLatex(trimmed)));
            }
        }
        return skills;
    }

    private List<String> extractItemTexts(List<String> lines) {
        List<String> items = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = ITEM_PREFIX.matcher(line);
            if (matcher.matches()) {
                items.add(stripLatex(matcher.group(1)));
            }
        }
        return items;
    }

    private TailoredResumeContent buildContent(String summary, List<String> skills, List<String> bullets) {
        String summaryText = summary.trim();
        if (summaryText.length() > 500) {
            summaryText = summaryText.substring(0, 500).trim();
        }
        return new TailoredResumeContent(summaryText, List.copyOf(skills), List.copyOf(bullets));
    }

    private String extractDocumentBody(String rawText) {
        int begin = rawText.indexOf("\\begin{document}");
        int end = rawText.indexOf("\\end{document}");
        if (begin >= 0) {
            begin += "\\begin{document}".length();
            if (end > begin) {
                return rawText.substring(begin, end);
            }
            return rawText.substring(begin);
        }
        return rawText;
    }

    private boolean isHeaderLine(String line) {
        return line.contains("\\textbf{\\Huge")
                || line.contains("\\hfill")
                || line.contains("\\LARGE")
                || line.contains("\\Huge");
    }

    private List<String> splitInlineList(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(text.split("[,;|]"))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .toList();
    }

    private String stripLatex(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String cleaned = input.replaceAll("(?m)%.*$", "");
        cleaned = cleaned.replaceAll("\\\\begin\\{(?:itemize|enumerate)\\}\\[[^\\]]*\\]", "");
        cleaned = cleaned.replaceAll("\\\\begin\\{(?:itemize|enumerate)\\}", "");
        cleaned = cleaned.replaceAll("\\\\end\\{(?:itemize|enumerate)\\}", "");
        cleaned = cleaned.replaceAll("\\\\(vspace|hspace|hfill|newline|\\\\)\\*?(\\[[^\\]]*\\])?\\{[^}]*\\}", " ");
        cleaned = cleaned.replaceAll("\\\\(vspace|hspace|hfill|newline|\\\\)\\*?", " ");

        for (int i = 0; i < 5; i++) {
            cleaned = cleaned.replaceAll("\\\\[a-zA-Z]+\\*?(\\[[^\\]]*\\])?\\{([^{}]*)\\}", "$2");
        }

        cleaned = cleaned.replaceAll("[{}]", " ");
        cleaned = cleaned.replaceAll("\\\\[a-zA-Z@]+\\*?", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }
}
