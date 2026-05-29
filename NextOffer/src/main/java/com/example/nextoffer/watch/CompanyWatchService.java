package com.example.nextoffer.watch;

import com.example.nextoffer.auth.AuthUserDetails;
import com.example.nextoffer.career.AtsType;
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

    public CompanyWatchService(CompanyWatchRepository companyWatchRepository) {
        this.companyWatchRepository = companyWatchRepository;
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
                request.atsType() == null ? AtsType.GREENHOUSE : request.atsType()
        );
        if (request.enabled() != null) {
            watch.setEnabled(request.enabled());
        }
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
        }
        if (request.boardToken() != null) {
            watch.setBoardToken(request.boardToken().isBlank() ? null : request.boardToken().trim());
        }
        if (request.atsType() != null) {
            watch.setAtsType(request.atsType());
        }
        if (request.enabled() != null) {
            watch.setEnabled(request.enabled());
        }
        return CompanyWatchResponse.from(companyWatchRepository.save(watch));
    }

    @Transactional
    public void delete(Long id, AuthUserDetails principal) {
        CompanyWatch watch = getOwnedWatch(id, principal);
        companyWatchRepository.delete(watch);
    }

    private CompanyWatch getOwnedWatch(Long id, AuthUserDetails principal) {
        return companyWatchRepository.findByIdAndUserId(id, principal.getUser().getId())
                .orElseThrow(() -> new WatchNotFoundException(id));
    }
}
