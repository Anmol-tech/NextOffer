package com.example.nextoffer.resume;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LatexResumeUpdaterTest {

    private static final String BASE_LATEX = """
            \\documentclass[11pt,letterpaper]{article}
            \\usepackage[margin=0.75in]{geometry}
            \\begin{document}

            {\\LARGE \\textbf{Jane Doe}}\\hfill jane@example.com

            \\vspace{6pt}
            Software engineering student with Java and React experience.

            \\section*{Skills}
            \\begin{itemize}[leftmargin=*, nosep]
            \\item Java
            \\item React
            \\item SQL
            \\end{itemize}

            \\section*{Experience}
            \\begin{itemize}[leftmargin=*, nosep]
            \\item Built a campus job board with Spring Boot
            \\item Implemented REST APIs with PostgreSQL
            \\end{itemize}

            \\section*{Education}
            \\begin{itemize}[leftmargin=*, nosep]
            \\item B.S. Computer Science, Example University
            \\end{itemize}

            \\end{document}
            """;

    private final LatexResumeUpdater updater = new LatexResumeUpdater();

    @Test
    void updatesSummarySkillsAndExperienceInPlace() {
        TailoredResumeContent tailored = new TailoredResumeContent(
                "Backend-focused student emphasizing Java, Spring Boot, and PostgreSQL.",
                java.util.List.of("Java", "Spring Boot", "PostgreSQL", "REST APIs"),
                java.util.List.of(
                        "Built a campus job board with Spring Boot and PostgreSQL",
                        "Delivered REST APIs consumed by a React frontend"
                )
        );

        String updated = updater.update(BASE_LATEX, tailored);

        assertTrue(updated.contains("Backend-focused student emphasizing Java, Spring Boot, and PostgreSQL."));
        assertTrue(updated.contains("\\item Java"));
        assertTrue(updated.contains("\\item React"));
        assertTrue(updated.contains("\\item SQL"));
        assertTrue(updated.contains("Built a campus job board with Spring Boot"));
        assertTrue(updated.contains("Implemented REST APIs with PostgreSQL"));
        assertTrue(updated.contains("\\section*{Education}"));
        assertTrue(updated.contains("B.S. Computer Science, Example University"));
    }

    @Test
    void preservesCommentSectionResumeFormat() throws Exception {
        Path resumePath = Path.of("storage/resumes/1/4/resume.tex");
        org.junit.jupiter.api.Assumptions.assumeTrue(Files.exists(resumePath));
        String base = Files.readString(resumePath);

        TailoredResumeContent tailored = new TailoredResumeContent(
                "Infrastructure-focused software engineer with Python, FastAPI, and cloud automation experience.",
                java.util.List.of("Python", "FastAPI", "Kubernetes", "AWS"),
                java.util.List.of(
                        "Built production Python/FastAPI services and internal developer tools",
                        "Engineered a serverless GCP Pub/Sub cloud runner"
                )
        );

        String updated = updater.update(base, tailored);

        assertTrue(updated.contains("%---------------- EDUCATION ----------------"));
        assertTrue(updated.contains("Santa Clara University"));
        assertTrue(updated.contains("%---------------- PROJECTS ----------------"));
        assertTrue(updated.contains("FormWhisper"));
        assertTrue(updated.contains("%---------------- ACHIEVEMENTS ----------------"));
        assertTrue(updated.contains("Sentient Arena"));
        assertTrue(updated.contains("%---------------- TECHNICAL SKILLS ----------------"));
        assertTrue(updated.contains("Programming/Scripting:"));
        assertTrue(updated.contains("Miller Center for Global Impact"));
        assertFalse(updated.contains("\\section*{Skills}"));
    }
}
