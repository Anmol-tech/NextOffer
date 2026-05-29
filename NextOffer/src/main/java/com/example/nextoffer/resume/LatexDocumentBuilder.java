package com.example.nextoffer.resume;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LatexDocumentBuilder {

    private final String template;

    public LatexDocumentBuilder() throws IOException {
        this.template = new ClassPathResource("resume/resume-template.tex")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    public String build(String fullName, String email, TailoredResumeContent content) {
        return template
                .replace("{{FULL_NAME}}", LatexEscaper.escape(fullName))
                .replace("{{HEADER_CONTACT}}", buildHeaderContact(email))
                .replace("{{SUMMARY}}", LatexEscaper.escape(content.summary()))
                .replace("{{EDUCATION}}", placeholder("Add your education in the base resume."))
                .replace("{{EXPERIENCE}}", buildExperience(content.experienceBullets()))
                .replace("{{PROJECTS}}", placeholder("Add your projects in the base resume."))
                .replace("{{ACHIEVEMENTS}}", placeholder("Add your achievements in the base resume."))
                .replace("{{TECHNICAL_SKILLS}}", buildTechnicalSkills(content.skills()));
    }

    private static String buildHeaderContact(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        String escaped = LatexEscaper.escape(email);
        return "\\href{mailto:" + escaped + "}{" + escaped + "}";
    }

    private static String buildExperience(List<String> bullets) {
        if (bullets.isEmpty()) {
            return placeholder("Add experience bullets in the base resume.");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\\begin{itemize}[label=-]\n");
        for (String bullet : bullets) {
            builder.append("    \\item ").append(LatexEscaper.escape(bullet)).append('\n');
        }
        builder.append("\\end{itemize}");
        return builder.toString();
    }

    private static String buildTechnicalSkills(List<String> skills) {
        if (skills.isEmpty()) {
            return placeholder("Add skills in the base resume.");
        }
        String joined = skills.stream()
                .map(LatexEscaper::escape)
                .collect(Collectors.joining(", "));
        return "\\textbf{Skills:} " + joined;
    }

    private static String placeholder(String message) {
        return "{\\small\\textit{" + LatexEscaper.escape(message) + "}}";
    }
}
