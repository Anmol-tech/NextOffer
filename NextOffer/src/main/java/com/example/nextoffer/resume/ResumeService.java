package com.example.nextoffer.resume;

import com.example.nextoffer.auth.AuthUserDetails;
import com.example.nextoffer.job.JobPosting;
import com.example.nextoffer.job.JobPostingNotFoundException;
import com.example.nextoffer.job.JobPostingRepository;
import com.example.nextoffer.resume.dto.BaseResumeResponse;
import com.example.nextoffer.resume.dto.TailoredResumeDetailResponse;
import com.example.nextoffer.resume.dto.TailoredResumeResponse;
import com.example.nextoffer.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
public class ResumeService {

    private final BaseResumeRepository baseResumeRepository;
    private final TailoredResumeRepository tailoredResumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeTextParser resumeTextParser;
    private final ResumeTailoringFacade resumeTailoringFacade;
    private final LatexDocumentBuilder latexDocumentBuilder;
    private final LatexResumeUpdater latexResumeUpdater;
    private final LatexFinalizeService latexFinalizeService;
    private final PdfCompileService pdfCompileService;
    private final ObjectMapper objectMapper;

    public ResumeService(
            BaseResumeRepository baseResumeRepository,
            TailoredResumeRepository tailoredResumeRepository,
            JobPostingRepository jobPostingRepository,
            ResumeTextParser resumeTextParser,
            ResumeTailoringFacade resumeTailoringFacade,
            LatexDocumentBuilder latexDocumentBuilder,
            LatexResumeUpdater latexResumeUpdater,
            LatexFinalizeService latexFinalizeService,
            PdfCompileService pdfCompileService,
            ObjectMapper objectMapper) {
        this.baseResumeRepository = baseResumeRepository;
        this.tailoredResumeRepository = tailoredResumeRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.resumeTextParser = resumeTextParser;
        this.resumeTailoringFacade = resumeTailoringFacade;
        this.latexDocumentBuilder = latexDocumentBuilder;
        this.latexResumeUpdater = latexResumeUpdater;
        this.latexFinalizeService = latexFinalizeService;
        this.pdfCompileService = pdfCompileService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BaseResumeResponse saveBaseResume(AuthUserDetails principal, String rawText) {
        User user = principal.getUser();
        BaseResume resume = baseResumeRepository.findByUserId(user.getId())
                .orElseGet(() -> new BaseResume(user, rawText));
        resume.setRawText(rawText);
        resume.setUpdatedAt(Instant.now());
        BaseResume saved = baseResumeRepository.save(resume);
        return toBaseResponse(saved);
    }

    @Transactional(readOnly = true)
    public BaseResumeResponse getBaseResume(AuthUserDetails principal) {
        return baseResumeRepository.findByUserId(principal.getUser().getId())
                .map(this::toBaseResponse)
                .orElseThrow(() -> new BaseResumeNotFoundException());
    }

    private BaseResumeResponse toBaseResponse(BaseResume resume) {
        return BaseResumeResponse.from(resume, resumeTextParser.detectFormat(resume.getRawText()));
    }

    @Transactional(readOnly = true)
    public List<TailoredResumeResponse> listTailored(AuthUserDetails principal) {
        return tailoredResumeRepository.findByUserIdOrderByCreatedAtDesc(principal.getUser().getId())
                .stream()
                .map(TailoredResumeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TailoredResumeDetailResponse getTailored(Long id, AuthUserDetails principal) {
        TailoredResume resume = getOwnedTailored(id, principal);
        return TailoredResumeDetailResponse.from(resume, objectMapper, detectBaseFormat(resume));
    }

    private ResumeSourceFormat detectBaseFormat(TailoredResume resume) {
        return resumeTextParser.detectFormat(resume.getBaseResume().getRawText());
    }

    @Transactional
    public TailoredResumeDetailResponse generateForJob(Long jobId, AuthUserDetails principal) {
        User user = principal.getUser();
        JobPosting job = jobPostingRepository.findByIdAndUserId(jobId, user.getId())
                .orElseThrow(() -> new JobPostingNotFoundException(jobId));
        BaseResume baseResume = baseResumeRepository.findByUserId(user.getId())
                .orElseThrow(BaseResumeNotFoundException::new);

        TailoredResumeContent parsed = resumeTextParser.parse(baseResume.getRawText());
        TailoredResumeContent tailored = resumeTailoringFacade.tailor(job.toDto(), parsed);

        TailoredResume entity = new TailoredResume(user, job, baseResume, writeJson(tailored));
        entity = tailoredResumeRepository.save(entity);

        ResumeSourceFormat sourceFormat = resumeTextParser.detectFormat(baseResume.getRawText());
        String latex = sourceFormat == ResumeSourceFormat.LATEX
                ? latexResumeUpdater.update(baseResume.getRawText(), tailored)
                : latexDocumentBuilder.build(user.getFullName(), user.getEmail(), tailored);
        latex = latexFinalizeService.finalizeLatex(latex);
        PdfCompileService.RenderResult rendered = pdfCompileService.render(user.getId(), entity.getId(), latex);

        entity.setLatexContent(rendered.latex());
        entity.setOutputStatus(rendered.status());
        if (rendered.pdfPath() != null) {
            entity.setPdfStoragePath(user.getId() + "/" + entity.getId() + "/resume.pdf");
        }
        entity = tailoredResumeRepository.save(entity);

        return TailoredResumeDetailResponse.from(entity, objectMapper, sourceFormat);
    }

    @Transactional
    public TailoredResumeDetailResponse compilePdf(Long id, AuthUserDetails principal) {
        TailoredResume resume = getOwnedTailored(id, principal);
        String latex = resume.getLatexContent();
        if (latex == null || latex.isBlank()) {
            throw new ResumeFileNotFoundException("LaTeX content not found");
        }

        User user = principal.getUser();
        latex = latexFinalizeService.finalizeLatex(latex);
        PdfCompileService.RenderResult rendered = pdfCompileService.recompileExisting(user.getId(), resume.getId(), latex);
        resume.setLatexContent(rendered.latex());
        resume.setOutputStatus(rendered.status());
        if (rendered.pdfPath() != null) {
            resume.setPdfStoragePath(user.getId() + "/" + resume.getId() + "/resume.pdf");
        }
        resume = tailoredResumeRepository.save(resume);
        return TailoredResumeDetailResponse.from(resume, objectMapper, detectBaseFormat(resume));
    }

    @Transactional(readOnly = true)
    public DownloadPayload download(Long id, String format, AuthUserDetails principal) {
        TailoredResume resume = getOwnedTailored(id, principal);

        if ("pdf".equalsIgnoreCase(format)) {
            if (resume.getPdfStoragePath() == null) {
                throw new ResumeFileNotFoundException("PDF not available. Download LaTeX instead or install pdflatex.");
            }
            Path pdfPath = pdfCompileService.resolvePdfPath(resume.getPdfStoragePath());
            if (!Files.exists(pdfPath)) {
                throw new ResumeFileNotFoundException("PDF file missing on disk");
            }
            return new DownloadPayload(new FileSystemResource(pdfPath), MediaType.APPLICATION_PDF, "resume-" + id + ".pdf");
        }

        String latex = resume.getLatexContent();
        if (latex == null || latex.isBlank()) {
            throw new ResumeFileNotFoundException("LaTeX content not found");
        }
        return new DownloadPayload(
                new org.springframework.core.io.ByteArrayResource(latex.getBytes(StandardCharsets.UTF_8)),
                MediaType.TEXT_PLAIN,
                "resume-" + id + ".tex"
        );
    }

    private TailoredResume getOwnedTailored(Long id, AuthUserDetails principal) {
        return tailoredResumeRepository.findByIdAndUserId(id, principal.getUser().getId())
                .orElseThrow(() -> new TailoredResumeNotFoundException(id));
    }

    private String writeJson(TailoredResumeContent content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception ex) {
            throw new ResumeGenerationException("Failed to serialize tailored resume", ex);
        }
    }

    public record DownloadPayload(Resource resource, MediaType mediaType, String filename) {
    }
}
