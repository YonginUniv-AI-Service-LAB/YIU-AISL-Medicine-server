package com.aisl.mediroutine.service;

// =====================================================
// [담당 API]
//   POST /emails/verification-code/send   → 인증번호 발송 (UPSERT)
//   POST /emails/verification-code/verify → 인증번호 검증
//
// [만료 시간] 10분
// [만료 상태코드] 410 GONE
// =====================================================

import com.aisl.mediroutine.entity.EmailVerification;
import com.aisl.mediroutine.global.exception.CustomException;
import com.aisl.mediroutine.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    private static final int EXPIRY_MINUTES = 10;

    /**
     * POST /emails/verification-code/send
     * 인증번호 발송 — UPSERT 방식
     */
    @Transactional
    public void sendVerificationCode(String email, EmailVerification.Purpose purpose) {

        emailVerificationRepository.deleteByEmailAndPurpose(email, purpose);

        String code = RandomStringUtils.randomNumeric(6);

        emailVerificationRepository.save(
                EmailVerification.builder()
                        .email(email)
                        .code(code)
                        .purpose(purpose)
                        .build()
        );

        sendEmail(email, code, purpose);
    }

    /**
     * POST /emails/verification-code/verify
     * 인증번호 검증 — 성공 시 레코드는 유지 (signup/reset 시 최종 재검증에 사용)
     */
    @Transactional(readOnly = true)
    public void verifyCode(String email, String code, EmailVerification.Purpose purpose) {
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.BAD_REQUEST, "인증번호를 먼저 요청해주세요."));

        checkExpiry(verification);
        checkCode(verification, code);

        // verify API에서는 레코드를 삭제하지 않습니다.
        // 이유: signup/password-reset에서 최종 재검증 시 다시 필요하기 때문입니다.
    }

    /**
     * 내부용: signup, password-reset에서 최종 재검증 후 레코드 삭제
     * 검증 성공 시 해당 레코드를 삭제해 재사용을 방지합니다.
     */
    @Transactional
    public void verifyAndDelete(String email, String code, EmailVerification.Purpose purpose) {
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."));

        checkExpiry(verification);
        checkCode(verification, code);

        emailVerificationRepository.delete(verification);
    }

    // ===== private 내부 메서드 =====

    private void checkExpiry(EmailVerification verification) {
        if (verification.getCreatedAt().plusMinutes(EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
            throw new CustomException(HttpStatus.GONE, "인증번호가 만료되었습니다.");
            // 410 GONE: HttpStatus.GONE
        }
    }

    private void checkCode(EmailVerification verification, String code) {
        if (!verification.getCode().equals(code)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다.");
        }
    }

    private void sendEmail(String to, String code, EmailVerification.Purpose purpose) {
        String subject = purpose == EmailVerification.Purpose.SIGNUP
                ? "[Mediroutine] 회원가입 이메일 인증번호"
                : "[Mediroutine] 비밀번호 재설정 인증번호";

        String text = String.format(
                "인증번호: %s\n\n해당 인증번호는 %d분 후 만료됩니다.",
                code, EXPIRY_MINUTES
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (MailException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다.");
        }
    }
}