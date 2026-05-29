package com.example.nextoffer.resume;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeTextParserTest {

    private final ResumeTextParser parser = new ResumeTextParser(new LatexResumeParser());

    @Test
    void parsesPlainTextResume() {
        String raw = """
                Software engineering student with Java and React experience.
                Skills: Java, React, SQL, Git
                - Built a campus job board with Spring Boot
                - Implemented REST APIs with PostgreSQL
                """;

        TailoredResumeContent content = parser.parse(raw);

        assertEquals("Software engineering student with Java and React experience.", content.summary());
        assertTrue(content.skills().contains("Java"));
        assertTrue(content.skills().contains("React"));
        assertEquals(2, content.experienceBullets().size());
        assertEquals(ResumeSourceFormat.TEXT, parser.detectFormat(raw));
    }

    @Test
    void parsesLatexResume() {
        String raw = """
                \\documentclass[11pt,letterpaper]{article}
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

                \\end{document}
                """;

        TailoredResumeContent content = parser.parse(raw);

        assertEquals(ResumeSourceFormat.LATEX, parser.detectFormat(raw));
        assertTrue(content.summary().contains("Software engineering student"));
        assertTrue(content.skills().contains("Java"));
        assertTrue(content.skills().contains("React"));
        assertEquals(2, content.experienceBullets().size());
        assertFalse(content.experienceBullets().getFirst().isBlank());
    }
}
