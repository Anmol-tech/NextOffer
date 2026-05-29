package com.example.nextoffer.watch;

import com.example.nextoffer.auth.AuthUserDetails;
import com.example.nextoffer.watch.dto.CompanyWatchResponse;
import com.example.nextoffer.watch.dto.CreateCompanyWatchRequest;
import com.example.nextoffer.watch.dto.PollWatchResponse;
import com.example.nextoffer.watch.dto.UpdateCompanyWatchRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/watches")
public class CompanyWatchController {

    private final CompanyWatchService companyWatchService;
    private final JobObserverService jobObserverService;

    public CompanyWatchController(CompanyWatchService companyWatchService, JobObserverService jobObserverService) {
        this.companyWatchService = companyWatchService;
        this.jobObserverService = jobObserverService;
    }

    @GetMapping
    public List<CompanyWatchResponse> list(@AuthenticationPrincipal AuthUserDetails principal) {
        return companyWatchService.listForUser(principal);
    }

    @GetMapping("/{id}")
    public CompanyWatchResponse get(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return companyWatchService.getForUser(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyWatchResponse create(
            @Valid @RequestBody CreateCompanyWatchRequest request,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return companyWatchService.create(request, principal);
    }

    @PutMapping("/{id}")
    public CompanyWatchResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyWatchRequest request,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return companyWatchService.update(id, request, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthUserDetails principal) {
        companyWatchService.delete(id, principal);
    }

    @PostMapping("/{id}/poll")
    public PollWatchResponse poll(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUserDetails principal) {
        return PollWatchResponse.from(jobObserverService.pollWatch(id, principal.getUser().getId()));
    }
}
