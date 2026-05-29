package com.example.nextoffer.resume;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LatexSanitizerTest {

    private final LatexSanitizer sanitizer = new LatexSanitizer();

    @Test
    void removesOrphanSummaryBeforeResumeHeader() {
        String latex = """
                \\documentclass{resume}
                \\begin{document}

                Orphan summary line that should be removed.
                %---------------- HEADER ----------------
                \\begin{center}
                    {\\textbf{\\Huge Jane Doe}}
                \\end{center}

                \\begin{rSection}{Summary}
                Tailored summary text.
                \\end{rSection}

                \\end{document}
                """;

        String sanitized = sanitizer.sanitize(latex);

        assertFalse(sanitized.contains("Orphan summary line that should be removed."));
        assertTrue(sanitized.contains("Tailored summary text."));
        assertTrue(sanitized.contains("\\begin{center}"));
    }

    @Test
    void keepsPlainSummaryForArticleTemplate() {
        String latex = """
                \\documentclass[11pt,letterpaper]{article}
                \\begin{document}

                \\section*{Summary}
                Plain summary line stays.

                \\end{document}
                """;

        String sanitized = sanitizer.sanitize(latex);

        assertTrue(sanitized.contains("Plain summary line stays."));
    }

    @Test
    void rejectsUnbalancedSections() {
        String latex = """
                \\documentclass{resume}
                \\begin{document}
                \\begin{rSection}{Experience}
                Missing end tag.
                \\end{document}
                """;

        assertThrows(ResumeGenerationException.class, () -> sanitizer.sanitize(latex));
    }

    @Test
    void sanitizesStoredResumeFormat() throws Exception {
        Path resumePath = Path.of("storage/resumes/1/5/resume.tex");
        org.junit.jupiter.api.Assumptions.assumeTrue(Files.exists(resumePath));

        String sanitized = sanitizer.sanitize(Files.readString(resumePath));

        assertFalse(sanitized.contains(
                "Software Engineer with 3+ years of production experience building backend services, automation workflows, and cloud data pipelines, emphasizing infrastructure tooling, reproducible systems, observability, and security-aware engineering to support efficient workplace space planning.\n%---------------- HEADER ----------------"));
        assertTrue(sanitized.contains("\\begin{rSection}{Summary}"));
    }
}
