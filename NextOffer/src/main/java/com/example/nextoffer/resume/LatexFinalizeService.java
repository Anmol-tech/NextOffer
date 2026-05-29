package com.example.nextoffer.resume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LatexFinalizeService {

    private static final Logger log = LoggerFactory.getLogger(LatexFinalizeService.class);

    private final LatexSanitizer latexSanitizer;
    private final PdfCompileService pdfCompileService;
    private final OpenAiClient openAiClient;
    private final boolean reviewEnabled;

    public LatexFinalizeService(
            LatexSanitizer latexSanitizer,
            PdfCompileService pdfCompileService,
            OpenAiClient openAiClient,
            @Value("${app.resume.latex-review:true}") boolean reviewEnabled) {
        this.latexSanitizer = latexSanitizer;
        this.pdfCompileService = pdfCompileService;
        this.openAiClient = openAiClient;
        this.reviewEnabled = reviewEnabled;
    }

    public String finalizeLatex(String latex) {
        String sanitized = latexSanitizer.sanitize(latex);
        PdfCompileService.CompileProbeResult probe = pdfCompileService.probeCompile(sanitized);

        if (probe.success()) {
            log.debug("LaTeX compile probe passed before finalize");
            return sanitized;
        }

        log.warn("LaTeX compile probe failed before finalize: {}", probe.summary());
        if (!reviewEnabled || !openAiClient.isEnabled()) {
            return sanitized;
        }

        try {
            String reviewed = openAiClient.reviewLatex(sanitized, probe.logExcerpt());
            String reSanitized = latexSanitizer.sanitize(reviewed);
            PdfCompileService.CompileProbeResult secondProbe = pdfCompileService.probeCompile(reSanitized);
            if (secondProbe.success()) {
                log.info("LaTeX review corrected compile issues before finalize");
                return reSanitized;
            }
            log.warn("LaTeX still failed compile probe after review: {}", secondProbe.summary());
            return reSanitized;
        } catch (Exception ex) {
            log.warn("LaTeX review failed, using sanitized output: {}", ex.getMessage());
            return sanitized;
        }
    }
}
