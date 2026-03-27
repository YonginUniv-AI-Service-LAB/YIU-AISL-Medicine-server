package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndPurpose(
            String email,
            EmailVerification.Purpose purpose
    );

    void deleteByEmailAndPurpose(String email, EmailVerification.Purpose purpose);

    void deleteByEmail(String email);
}