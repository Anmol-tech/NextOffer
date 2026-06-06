package com.example.nextoffer.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TailoredResumeRepository extends JpaRepository<TailoredResume, Long> {

    @Query("""
            SELECT tr FROM TailoredResume tr
            JOIN FETCH tr.jobPosting jp
            WHERE tr.user.id = :userId
            ORDER BY tr.createdAt DESC
            """)
    List<TailoredResume> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("""
            SELECT tr FROM TailoredResume tr
            JOIN FETCH tr.jobPosting jp
            WHERE tr.id = :id AND tr.user.id = :userId
            """)
    Optional<TailoredResume> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    Optional<TailoredResume> findTopByJobPostingIdAndUserIdOrderByCreatedAtDesc(Long jobPostingId, Long userId);

    @Modifying
    @Query("DELETE FROM TailoredResume tr WHERE tr.jobPosting.companyWatch.id = :watchId")
    void deleteByJobPostingCompanyWatchId(@Param("watchId") Long watchId);
}
