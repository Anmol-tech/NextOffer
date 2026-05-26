package com.example.nextoffer.career;

/**
 * Flyweight — shared immutable branding for a company.
 */
public record CompanyBrandingFlyweight(String companyKey, String displayName, String logoUrl) {
}
