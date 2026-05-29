package com.example.nextoffer.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaseResumeRepository extends JpaRepository<BaseResume, Long> {

    Optional<BaseResume> findByUserId(Long userId);
}
