package com.example.nextoffer.resume;

import com.example.nextoffer.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "base_resumes")
@Getter
@Setter
@NoArgsConstructor
public class BaseResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public BaseResume(User user, String rawText) {
        this.user = user;
        this.rawText = rawText;
    }
}
