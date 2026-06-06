package com.example.nextoffer.watch;

import com.example.nextoffer.auth.AuthUserDetails;
import com.example.nextoffer.career.AtsType;
import com.example.nextoffer.career.AtsTypeDetector;
import com.example.nextoffer.job.JobPostingRepository;
import com.example.nextoffer.resume.TailoredResumeRepository;
import com.example.nextoffer.user.User;
import com.example.nextoffer.watch.dto.CompanyWatchResponse;
import com.example.nextoffer.watch.dto.CreateCompanyWatchRequest;
import com.example.nextoffer.watch.dto.UpdateCompanyWatchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyWatchService {

    private final CompanyWatchRepository companyWatchRepository;
    private final JobPostingRepository jobPostingRepository;
    private final TailoredResumeRepository tailoredResumeRepository;

    public CompanyWatchService(
            CompanyWatchRepository companyWatchRepository,
            JobPostingRepository jobPostingRepository,
            TailoredResumeRepository tailoredResumeRepository) {
        this.companyWatchRepository = companyWatchRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.tailoredResumeRepository = tailoredResumeRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyWatchResponse> listForUser(AuthUserDetails principal) {
        return companyWatchRepository.findByUserIdOrderByCreatedAtDesc(principal.getUser().getId())
                .stream()
                .map(CompanyWatchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyWatchResponse getForUser(Long id, AuthUserDetails principal) {
        return CompanyWatchResponse.from(getOwnedWatch(id, principal));
    }

    @Transactional
    public CompanyWatchResponse create(CreateCompanyWatchRequest request, AuthUserDetails principal) {
        User user = principal.getUser();
        CompanyWatch watch = new CompanyWatch(
                user,
                request.companyName().trim(),
                request.careerPageUrl().trim(),
                request.boardToken(),
                AtsTypeDetector.resolve(request.atsType(), request.careerPageUrl())
        );
        if (request.enabled() != null) {
            watch.setEnabled(request.enabled());
        }
        applyFilters(watch, request.locationFilter(), request.keywordFilter(), request.departmentFilter());
        return CompanyWatchResponse.from(companyWatchRepository.save(watch));
    }

    @Transactional
    public CompanyWatchResponse update(Long id, UpdateCompanyWatchRequest request, AuthUserDetails principal) {
        CompanyWatch watch = getOwnedWatch(id, principal);
        if (request.companyName() != null) {
            watch.setCompanyName(request.companyName().trim());
        }
        if (request.careerPageUrl() != null) {
            watch.setCareerPageUrl(request.careerPageUrl().trim());
            watch.setAtsType(AtsTypeDetector.resolve(watch.getAtsType(), watch.getCareerPageUrl()));
        }
        if (request.boardToken() != null) {
            watch.setBoardToken(request.boardToken().isBlank() ? null : request.boardToken().trim());
        }
        if (request.atsType() != null) {
            watch.setAtsType(AtsTypeDetector.resolve(request.atsType(), watch.getCareerPageUrl()));
        }
        if (request.enabled() != null) {
            watch.setEnabled(request.enabled());
        }
        if (request.locationFilter() != null) {
            watch.setLocationFilter(normalizeFilter(request.locationFilter()));
        }
        if (request.keywordFilter() != null) {
            watch.setKeywordFilter(normalizeFilter(request.keywordFilter()));
        }
        if (request.departmentFilter() != null) {
            watch.setDepartmentFilter(normalizeFilter(request.departmentFilter()));
        }
        return CompanyWatchResponse.from(companyWatchRepository.save(watch));
    }

    private static void applyFilters(CompanyWatch watch, String location, String keywords, String department) {
        watch.setLocationFilter(normalizeFilter(location));
        watch.setKeywordFilter(normalizeFilter(keywords));
        watch.setDepartmentFilter(normalizeFilter(department));
    }

    private static String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @Transactional
    public void delete(Long id, AuthUserDetails principal) {
        CompanyWatch watch = getOwnedWatch(id, principal);
        Long watchId = watch.getId();
        tailoredResumeRepository.deleteByJobPostingCompanyWatchId(watchId);
        jobPostingRepository.deleteByCompanyWatchId(watchId);
        companyWatchRepository.delete(watch);
    }

    private CompanyWatch getOwnedWatch(Long id, AuthUserDetails principal) {
        return companyWatchRepository.findByIdAndUserId(id, principal.getUser().getId())
                .orElseThrow(() -> new WatchNotFoundException(id));
    }
}
