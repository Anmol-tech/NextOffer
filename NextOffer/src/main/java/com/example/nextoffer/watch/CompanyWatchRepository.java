package com.example.nextoffer.watch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyWatchRepository extends JpaRepository<CompanyWatch, Long> {

    List<CompanyWatch> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CompanyWatch> findByIdAndUserId(Long id, Long userId);

    List<CompanyWatch> findByEnabledTrue();
}
