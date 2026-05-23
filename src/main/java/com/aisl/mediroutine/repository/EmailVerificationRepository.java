package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndPurpose(
            String email,
            EmailVerification.Purpose purpose
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmailVerification e WHERE e.email = :email AND e.purpose = :purpose")
    void deleteByEmailAndPurpose(@Param("email") String email, @Param("purpose") EmailVerification.Purpose purpose);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmailVerification e WHERE e.email = :email")
    void deleteByEmail(@Param("email") String email);
}