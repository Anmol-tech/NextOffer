package com.example.nextoffer.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    boolean existsByCompanyWatchIdAndExternalId(Long companyWatchId, String externalId);

    @Query("SELECT jp.externalId FROM JobPosting jp WHERE jp.companyWatch.id = :watchId")
    Set<String> findExternalIdsByCompanyWatchId(@Param("watchId") Long watchId);

    @Query("""
            SELECT jp FROM JobPosting jp
            JOIN FETCH jp.companyWatch cw
            WHERE cw.user.id = :userId
            ORDER BY jp.firstSeenAt DESC
            """)
    List<JobPosting> findByUserIdOrderByFirstSeenAtDesc(@Param("userId") Long userId);

    @Query("""
            SELECT jp FROM JobPosting jp
            JOIN FETCH jp.companyWatch cw
            WHERE cw.user.id = :userId AND jp.id = :jobId
            """)
    Optional<JobPosting> findByIdAndUserId(@Param("jobId") Long jobId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM JobPosting jp WHERE jp.companyWatch.id = :watchId")
    void deleteByCompanyWatchId(@Param("watchId") Long watchId);
}
