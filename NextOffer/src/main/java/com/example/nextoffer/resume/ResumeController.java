package com.example.nextoffer.resume;

import com.example.nextoffer.auth.AuthUserDetails;
import com.example.nextoffer.resume.dto.BaseResumeResponse;
import com.example.nextoffer.resume.dto.SaveBaseResumeRequest;
import com.example.nextoffer.resume.dto.TailoredResumeDetailResponse;
import com.example.nextoffer.resume.dto.TailoredResumeResponse;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PutMapping("/base")
    public BaseResumeResponse saveBase(
            @Valid @RequestBody SaveBaseResumeRequest request,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return resumeService.saveBaseResume(principal, request.rawText());
    }

    @GetMapping("/base")
    public BaseResumeResponse getBase(@AuthenticationPrincipal AuthUserDetails principal) {
        return resumeService.getBaseResume(principal);
    }

    @GetMapping("/tailored")
    public List<TailoredResumeResponse> listTailored(@AuthenticationPrincipal AuthUserDetails principal) {
        return resumeService.listTailored(principal);
    }

    @GetMapping("/tailored/{id}")
    public TailoredResumeDetailResponse getTailored(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return resumeService.getTailored(id, principal);
    }

    @PostMapping("/tailored/{id}/compile-pdf")
    public TailoredResumeDetailResponse compilePdf(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return resumeService.compilePdf(id, principal);
    }

    @GetMapping("/tailored/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> download(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String format,
            @AuthenticationPrincipal AuthUserDetails principal) {
        ResumeService.DownloadPayload payload = resumeService.download(id, format, principal);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(payload.filename())
                        .build()
                        .toString())
                .contentType(payload.mediaType())
                .body(payload.resource());
    }
}
