package com.example.nextoffer.resume;

import com.example.nextoffer.job.JobPosting;
import com.example.nextoffer.user.User;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tailored_resumes")
@Getter
@Setter
@NoArgsConstructor
public class TailoredResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_resume_id", nullable = false)
    private BaseResume baseResume;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    @Column(columnDefinition = "TEXT")
    private String latexContent;

    @Column(length = 512)
    private String pdfStoragePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResumeOutputStatus outputStatus = ResumeOutputStatus.LATEX_ONLY;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TailoredResume(User user, JobPosting jobPosting, BaseResume baseResume, String contentJson) {
        this.user = user;
        this.jobPosting = jobPosting;
        this.baseResume = baseResume;
        this.contentJson = contentJson;
    }
}
