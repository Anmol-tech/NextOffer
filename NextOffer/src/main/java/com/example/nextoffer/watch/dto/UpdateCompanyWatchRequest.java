package com.example.nextoffer.watch.dto;

import com.example.nextoffer.career.AtsType;
import jakarta.validation.constraints.Size;

public record UpdateCompanyWatchRequest(
        @Size(max = 200) String companyName,
        @Size(max = 2048) String careerPageUrl,
        @Size(max = 128) String boardToken,
        AtsType atsType,
        Boolean enabled,
        @Size(max = 512) String locationFilter,
        @Size(max = 512) String keywordFilter,
        @Size(max = 512) String departmentFilter
) {
}
