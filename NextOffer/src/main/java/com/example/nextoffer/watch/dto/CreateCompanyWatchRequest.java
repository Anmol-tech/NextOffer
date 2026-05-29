package com.example.nextoffer.watch.dto;

import com.example.nextoffer.career.AtsType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyWatchRequest(
        @NotBlank @Size(max = 200) String companyName,
        @NotBlank @Size(max = 2048) String careerPageUrl,
        @Size(max = 128) String boardToken,
        AtsType atsType,
        Boolean enabled
) {
}
