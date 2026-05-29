package com.example.nextoffer.resume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class PdfCompileService {

    private static final Logger log = LoggerFactory.getLogger(PdfCompileService.class);
    private static final Pattern RESUME_DOCUMENT_CLASS = Pattern.compile("\\\\documentclass\\s*(?:\\[[^\\]]*\\])?\\s*\\{resume\\}");
    private static final String LATEX_SUPPORT_RESOURCE_PATTERN = "classpath:resume/*.cls";

    private final Path storageRoot;
    private final boolean compileEnabled;

    public PdfCompileService(@Value("${app.resume.storage-path:storage/resumes}") String storagePath,
                             @Value("${app.resume.compile-pdf:true}") boolean compileEnabled) {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
        this.compileEnabled = compileEnabled;
    }

    public RenderResult render(Long userId, Long tailoredResumeId, String latex) {
        try {
            Path dir = storageRoot.resolve(String.valueOf(userId)).resolve(String.valueOf(tailoredResumeId));
            Files.createDirectories(dir);
            Path texFile = dir.resolve("resume.tex");
            Files.writeString(texFile, latex);

            if (compileEnabled && isPdfLatexAvailable()) {
                copyLatexSupportFiles(dir, latex);
                Path pdf = compilePdf(dir, texFile);
                if (pdf != null) {
                    return new RenderResult(latex, pdf, ResumeOutputStatus.PDF_READY);
                }
                log.warn("pdflatex did not produce resume.pdf for tailored resume {}", tailoredResumeId);
            }

            return new RenderResult(latex, null, ResumeOutputStatus.LATEX_ONLY);
        } catch (Exception ex) {
            throw new ResumeGenerationException("Failed to render resume files", ex);
        }
    }

    public RenderResult recompileExisting(Long userId, Long tailoredResumeId, String latex) {
        return render(userId, tailoredResumeId, latex);
    }

    public CompileProbeResult probeCompile(String latex) {
        if (!compileEnabled || !isPdfLatexAvailable()) {
            return CompileProbeResult.skipped("pdflatex unavailable");
        }

        try {
            Path dir = Files.createTempDirectory(storageRoot, "latex-probe-");
            try {
                Path texFile = dir.resolve("resume.tex");
                Files.writeString(texFile, latex);
                copyLatexSupportFiles(dir, latex);
                String output = runPdflatex(dir, texFile);
                Path pdf = dir.resolve("resume.pdf");
                boolean success = Files.exists(pdf) && Files.size(pdf) > 0;
                if (success) {
                    return CompileProbeResult.passed();
                }
                return CompileProbeResult.failed(summarizeProbeFailure(output));
            } finally {
                deleteDirectory(dir);
            }
        } catch (Exception ex) {
            log.debug("LaTeX compile probe failed: {}", ex.getMessage());
            return CompileProbeResult.failed(ex.getMessage());
        }
    }

    public Path resolvePdfPath(String pdfStoragePath) {
        return storageRoot.resolve(pdfStoragePath).normalize();
    }

    private void copyLatexSupportFiles(Path dir, String latex) throws IOException {
        if (!needsResumeClass(latex)) {
            return;
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(LATEX_SUPPORT_RESOURCE_PATTERN);
        for (Resource resource : resources) {
            if (!resource.isReadable() || resource.getFilename() == null) {
                continue;
            }
            Path target = dir.resolve(resource.getFilename());
            try (InputStream input = resource.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static boolean needsResumeClass(String latex) {
        return latex != null && RESUME_DOCUMENT_CLASS.matcher(latex).find();
    }

    private Path compilePdf(Path dir, Path texFile) throws IOException, InterruptedException {
        String output = runPdflatex(dir, texFile);
        Path pdf = dir.resolve("resume.pdf");
        if (Files.exists(pdf) && Files.size(pdf) > 0) {
            runPdflatex(dir, texFile);
            return pdf;
        }
        if (output != null && !output.isBlank()) {
            log.warn("pdflatex output for {}:\n{}", texFile, truncate(output, 4000));
        }
        return null;
    }

    private String runPdflatex(Path dir, Path texFile) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                "-output-directory", dir.toString(),
                texFile.getFileName().toString())
                .directory(dir.toFile())
                .redirectErrorStream(true)
                .start();
        boolean finished = process.waitFor(90, TimeUnit.SECONDS);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!finished) {
            process.destroyForcibly();
            log.warn("pdflatex timed out for {}", texFile);
            return output;
        }
        if (process.exitValue() != 0) {
            log.debug("pdflatex exited with code {} for {}", process.exitValue(), texFile);
        }
        return output;
    }

    private static String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "\n... (truncated)";
    }

    private static String summarizeProbeFailure(String output) {
        if (output == null || output.isBlank()) {
            return "pdflatex did not produce a PDF";
        }

        Matcher matcher = Pattern.compile("(?m)^!(?: LaTeX Error:| Emergency stop\\.) .*").matcher(output);
        StringBuilder errors = new StringBuilder();
        while (matcher.find() && errors.length() < 2000) {
            if (!errors.isEmpty()) {
                errors.append('\n');
            }
            errors.append(matcher.group().trim());
        }

        if (!errors.isEmpty()) {
            return errors.toString();
        }

        return truncate(output, 2000);
    }

    private static void deleteDirectory(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    log.debug("Failed to delete probe file {}: {}", path, ex.getMessage());
                }
            });
        } catch (IOException ex) {
            log.debug("Failed to clean probe directory {}: {}", dir, ex.getMessage());
        }
    }

    private boolean isPdfLatexAvailable() {
        try {
            Process process = new ProcessBuilder("pdflatex", "--version").start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public record RenderResult(String latex, Path pdfPath, ResumeOutputStatus status) {
    }

    public record CompileProbeResult(boolean success, boolean skipped, String summary, String logExcerpt) {
        static CompileProbeResult passed() {
            return new CompileProbeResult(true, false, "ok", "");
        }

        static CompileProbeResult skipped(String reason) {
            return new CompileProbeResult(true, true, reason, "");
        }

        static CompileProbeResult failed(String logExcerpt) {
            return new CompileProbeResult(false, false, logExcerpt, logExcerpt);
        }
    }
}
