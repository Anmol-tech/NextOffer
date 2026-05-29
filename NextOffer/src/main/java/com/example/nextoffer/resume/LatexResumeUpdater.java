package com.example.nextoffer.resume;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LatexResumeUpdater {

    private static final Pattern BEGIN_LIST = Pattern.compile("\\\\begin\\{(itemize|enumerate)\\}");
    private static final Pattern ITEM_PREFIX = Pattern.compile("^(\\s*)\\\\item(?:\\[[^\\]]*\\])?\\s*(.*)$");

    public String update(String rawLatex, TailoredResumeContent content) {
        if (rawLatex == null || rawLatex.isBlank()) {
            throw new ResumeGenerationException("LaTeX base resume is empty");
        }

        int docBegin = rawLatex.indexOf("\\begin{document}");
        if (docBegin < 0) {
            throw new ResumeGenerationException("LaTeX base resume must contain \\begin{document}");
        }

        int bodyStart = docBegin + "\\begin{document}".length();
        int docEnd = rawLatex.indexOf("\\end{document}", bodyStart);
        String preamble = rawLatex.substring(0, bodyStart);
        String body = docEnd > bodyStart ? rawLatex.substring(bodyStart, docEnd) : rawLatex.substring(bodyStart);
        String postamble = docEnd > bodyStart ? rawLatex.substring(docEnd) : "";

        return preamble + updateBody(body, content) + postamble;
    }

    private String updateBody(String body, TailoredResumeContent content) {
        if (!LatexSectionScanner.hasExplicitSections(body)) {
            return updateLegacySectionBody(body, content);
        }

        List<LatexSectionScanner.Section> sections = LatexSectionScanner.splitBody(body);
        List<String> rebuilt = new ArrayList<>();

        for (LatexSectionScanner.Section section : sections) {
            rebuilt.addAll(updateSection(section, content));
        }

        return String.join("\n", rebuilt);
    }

    private List<String> updateSection(LatexSectionScanner.Section section, TailoredResumeContent content) {
        return switch (LatexSectionScanner.classify(section.title())) {
            case PRELUDE -> replacePreSectionSummary(section.lines(), content.summary());
            case SUMMARY -> replaceSectionText(section.lines(), content.summary());
            case SKILLS -> updateSkillsSection(section.lines(), content.skills());
            case EXPERIENCE -> reorderItemSection(section.lines(), content.experienceBullets());
            case PROJECTS, OTHER -> section.lines();
        };
    }

    private String updateLegacySectionBody(String body, TailoredResumeContent content) {
        List<String> lines = new ArrayList<>(List.of(body.split("\\R", -1)));
        int index = 0;
        List<String> preSection = new ArrayList<>();

        while (index < lines.size() && !isLatexSectionHeader(lines.get(index))) {
            preSection.add(lines.get(index));
            index++;
        }

        List<String> rebuilt = new ArrayList<>(replacePreSectionSummary(preSection, content.summary()));

        while (index < lines.size()) {
            String sectionLine = lines.get(index);
            if (!isLatexSectionHeader(sectionLine)) {
                rebuilt.add(sectionLine);
                index++;
                continue;
            }

            String sectionTitle = extractSectionTitle(sectionLine);
            List<String> sectionLines = new ArrayList<>();
            sectionLines.add(sectionLine);
            index++;

            while (index < lines.size() && !isLatexSectionHeader(lines.get(index))) {
                sectionLines.add(lines.get(index));
                index++;
            }

            rebuilt.addAll(updateSection(new LatexSectionScanner.Section(sectionTitle, sectionLines), content));
        }

        return String.join("\n", rebuilt);
    }

    private List<String> updateSkillsSection(List<String> sectionLines, List<String> skills) {
        if (containsListEnvironment(sectionLines)) {
            return replaceSectionItemsPreservingExtras(sectionLines, skills);
        }
        return sectionLines;
    }

    private List<String> reorderItemSection(List<String> sectionLines, List<String> preferredBullets) {
        List<String> originalItems = extractItemContents(sectionLines);
        if (originalItems.isEmpty()) {
            return sectionLines;
        }

        List<String> merged = BulletOrderMerger.mergeOrder(preferredBullets, originalItems);
        return replaceItemContentsInPlace(sectionLines, merged);
    }

    private List<String> replaceSectionItemsPreservingExtras(List<String> sectionLines, List<String> items) {
        int beginIdx = -1;
        int endIdx = -1;
        String listEnv = null;

        for (int i = 0; i < sectionLines.size(); i++) {
            String trimmed = sectionLines.get(i).trim();
            Matcher begin = BEGIN_LIST.matcher(trimmed);
            if (beginIdx < 0 && begin.find()) {
                beginIdx = i;
                listEnv = begin.group(1);
                continue;
            }
            if (beginIdx >= 0 && listEnv != null && trimmed.startsWith("\\end{" + listEnv + "}")) {
                endIdx = i;
                break;
            }
        }

        if (beginIdx < 0 || endIdx < 0) {
            return reorderItemSection(sectionLines, items);
        }

        List<String> originalItems = extractItemContents(sectionLines.subList(beginIdx + 1, endIdx));
        List<String> merged = BulletOrderMerger.mergeOrder(items, originalItems);

        List<String> updated = new ArrayList<>(sectionLines.subList(0, beginIdx + 1));
        for (String item : merged) {
            updated.add("\\item " + formatItemLineContent(item));
        }
        updated.addAll(sectionLines.subList(endIdx, sectionLines.size()));
        return updated;
    }

    private List<String> replaceItemContentsInPlace(List<String> lines, List<String> orderedContents) {
        List<String> result = new ArrayList<>();
        int itemIndex = 0;

        for (String line : lines) {
            Matcher matcher = ITEM_PREFIX.matcher(line);
            if (matcher.matches()) {
                if (itemIndex < orderedContents.size()) {
                    result.add(matcher.group(1) + "\\item " + orderedContents.get(itemIndex));
                    itemIndex++;
                } else {
                    result.add(line);
                }
            } else {
                result.add(line);
            }
        }

        return result;
    }

    private List<String> extractItemContents(List<String> lines) {
        List<String> items = new ArrayList<>();
        for (String line : lines) {
            Matcher matcher = ITEM_PREFIX.matcher(line);
            if (matcher.matches()) {
                items.add(matcher.group(2).trim());
            }
        }
        return items;
    }

    private boolean containsListEnvironment(List<String> lines) {
        return lines.stream().anyMatch(line -> BEGIN_LIST.matcher(line.trim()).find());
    }

    private List<String> replacePreSectionSummary(List<String> preSectionLines, String summary) {
        List<String> result = new ArrayList<>();
        boolean summaryInserted = false;

        for (String line : preSectionLines) {
            String trimmed = line.trim();
            if (trimmed.isBlank() || trimmed.startsWith("%") || isHeaderLine(trimmed) || isStructuralLine(trimmed)) {
                result.add(line);
                continue;
            }

            if (!summaryInserted) {
                if (!summary.isBlank()) {
                    result.add(LatexEscaper.escape(summary));
                }
                summaryInserted = true;
                continue;
            }

            result.add(line);
        }

        if (!summaryInserted && !summary.isBlank()) {
            if (!result.isEmpty() && !result.getLast().isBlank()) {
                result.add("");
            }
            result.add(LatexEscaper.escape(summary));
        }

        return result;
    }

    private List<String> replaceSectionText(List<String> sectionLines, String text) {
        if (sectionLines.isEmpty()) {
            return sectionLines;
        }

        List<String> result = new ArrayList<>();
        result.add(sectionLines.getFirst());
        boolean textInserted = false;

        for (int i = 1; i < sectionLines.size(); i++) {
            String trimmed = sectionLines.get(i).trim();
            if (isItemLine(trimmed)
                    || trimmed.startsWith("\\begin{")
                    || trimmed.startsWith("\\end{")
                    || trimmed.startsWith("\\textbf{")
                    || trimmed.startsWith("\\textit{")
                    || trimmed.startsWith("\\href{")) {
                result.add(sectionLines.get(i));
                continue;
            }
            if (trimmed.isBlank() || trimmed.startsWith("%")) {
                result.add(sectionLines.get(i));
                continue;
            }
            if (!textInserted) {
                if (!text.isBlank()) {
                    result.add(LatexEscaper.escape(text));
                }
                textInserted = true;
                continue;
            }
            result.add(sectionLines.get(i));
        }

        if (!textInserted && !text.isBlank()) {
            result.add(LatexEscaper.escape(text));
        }

        return result;
    }

    private boolean isLatexSectionHeader(String line) {
        return LatexSectionScanner.sectionTitleFromLine(line) != null;
    }

    private String extractSectionTitle(String line) {
        String title = LatexSectionScanner.sectionTitleFromLine(line);
        return title == null ? "" : title;
    }

    private boolean isItemLine(String line) {
        return ITEM_PREFIX.matcher(line).matches();
    }

    private boolean isHeaderLine(String line) {
        return line.contains("\\textbf")
                || line.contains("\\hfill")
                || line.contains("\\LARGE")
                || line.contains("\\Huge")
                || line.contains("\\large");
    }

    private String formatItemLineContent(String item) {
        if (item == null || item.isBlank()) {
            return "";
        }
        return item.contains("\\") ? item : LatexEscaper.escape(item);
    }

    private boolean isStructuralLine(String line) {
        return line.startsWith("\\vspace")
                || line.startsWith("\\hspace")
                || line.startsWith("\\vfill")
                || line.startsWith("\\newline")
                || line.startsWith("\\linebreak")
                || line.matches("\\\\\\s*");
    }
}
