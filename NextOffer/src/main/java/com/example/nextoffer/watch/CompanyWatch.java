package com.example.nextoffer.watch;

import com.example.nextoffer.career.AtsType;
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
@Table(name = "company_watches")
@Getter
@Setter
@NoArgsConstructor
public class CompanyWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, length = 2048)
    private String careerPageUrl;

    @Column(length = 128)
    private String boardToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AtsType atsType = AtsType.GREENHOUSE;

    @Column(nullable = false)
    private boolean enabled = true;

    private Instant lastCheckedAt;

    @Enumerated(EnumType.STRING)
    private ScanStatus lastScanStatus;

    @Column(length = 1024)
    private String lastErrorMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public CompanyWatch(User user, String companyName, String careerPageUrl, String boardToken, AtsType atsType) {
        this.user = user;
        this.companyName = companyName;
        this.careerPageUrl = careerPageUrl;
        this.boardToken = boardToken;
        this.atsType = atsType == null ? AtsType.GREENHOUSE : atsType;
    }
}
