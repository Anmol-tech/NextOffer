package com.example.nextoffer.resume;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ResumeTextParser {

    private static final Pattern BULLET = Pattern.compile("^[-*•]\\s+");

    private final LatexResumeParser latexResumeParser;

    public ResumeTextParser(LatexResumeParser latexResumeParser) {
        this.latexResumeParser = latexResumeParser;
    }

    public ResumeSourceFormat detectFormat(String rawText) {
        return ResumeSourceFormat.detect(rawText);
    }

    public TailoredResumeContent parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new TailoredResumeContent("", List.of(), List.of());
        }
        if (detectFormat(rawText) == ResumeSourceFormat.LATEX) {
            return latexResumeParser.parse(rawText);
        }
        return parsePlainText(rawText);
    }

    private TailoredResumeContent parsePlainText(String rawText) {
        List<String> lines = Arrays.stream(rawText.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        String summary = lines.isEmpty() ? "" : lines.getFirst();
        List<String> skills = new ArrayList<>();
        List<String> bullets = new ArrayList<>();
        boolean inSkills = false;

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String lower = line.toLowerCase(Locale.ROOT);

            if (lower.startsWith("skills:") || lower.equals("skills")) {
                inSkills = true;
                String inline = line.contains(":") ? line.substring(line.indexOf(':') + 1).trim() : "";
                if (!inline.isBlank()) {
                    skills.addAll(splitSkills(inline));
                }
                continue;
            }

            if (lower.startsWith("experience:") || lower.equals("experience")) {
                inSkills = false;
                continue;
            }

            if (BULLET.matcher(line).find()) {
                inSkills = false;
                bullets.add(BULLET.matcher(line).replaceFirst("").trim());
                continue;
            }

            if (inSkills) {
                skills.addAll(splitSkills(line));
            } else if (line.length() > 20) {
                bullets.add(line);
            }
        }

        if (bullets.isEmpty() && lines.size() > 1) {
            bullets.addAll(lines.subList(1, Math.min(lines.size(), 6)));
        }

        return new TailoredResumeContent(summary, List.copyOf(skills), List.copyOf(bullets));
    }

    private List<String> splitSkills(String line) {
        return Arrays.stream(line.split("[,;|]"))
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .toList();
    }
}
