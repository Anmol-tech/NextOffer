package com.example.nextoffer.watch.dto;

import com.example.nextoffer.career.AtsType;
import com.example.nextoffer.watch.CompanyWatch;
import com.example.nextoffer.watch.ScanStatus;

import java.time.Instant;

public record CompanyWatchResponse(
        Long id,
        String companyName,
        String careerPageUrl,
        String boardToken,
        AtsType atsType,
        boolean enabled,
        String locationFilter,
        String keywordFilter,
        String departmentFilter,
        Instant lastCheckedAt,
        ScanStatus lastScanStatus,
        String lastErrorMessage,
        Instant createdAt
) {
    public static CompanyWatchResponse from(CompanyWatch watch) {
        return new CompanyWatchResponse(
                watch.getId(),
                watch.getCompanyName(),
                watch.getCareerPageUrl(),
                watch.getBoardToken(),
                watch.getAtsType(),
                watch.isEnabled(),
                watch.getLocationFilter(),
                watch.getKeywordFilter(),
                watch.getDepartmentFilter(),
                watch.getLastCheckedAt(),
                watch.getLastScanStatus(),
                watch.getLastErrorMessage(),
                watch.getCreatedAt()
        );
    }
}
