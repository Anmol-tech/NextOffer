package com.example.nextoffer.job;

import com.example.nextoffer.tracker.ApplicationStatus;
import com.example.nextoffer.watch.CompanyWatch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "job_postings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_watch_id", "external_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_watch_id", nullable = false)
    private CompanyWatch companyWatch;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String title;

    private String location;

    @Column(nullable = false, length = 2048)
    private String applyUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant firstSeenAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false)
    private ApplicationStatus applicationStatus = ApplicationStatus.NEW;

    @Column(name = "status_updated_at", nullable = false)
    private Instant statusUpdatedAt = Instant.now();

    public JobPosting(CompanyWatch companyWatch, JobPostingDto dto) {
        this.companyWatch = companyWatch;
        this.externalId = dto.externalId();
        this.companyName = dto.companyName();
        this.title = dto.title();
        this.location = dto.location();
        this.applyUrl = dto.applyUrl();
        this.description = dto.description();
        this.firstSeenAt = dto.firstSeenAt() != null ? dto.firstSeenAt() : Instant.now();
    }

    public JobPostingDto toDto() {
        return new JobPostingDto(
                externalId,
                companyName,
                title,
                location,
                applyUrl,
                description,
                firstSeenAt
        );
    }
}
