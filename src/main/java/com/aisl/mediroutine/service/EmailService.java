package com.aisl.mediroutine.service;

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
     * 인증번호 발송 (UPSERT)
     */
    @Transactional
    public void sendVerificationCode(String email, EmailVerification.Purpose purpose) {

        // 기존 데이터 삭제 (중복 방지)
        emailVerificationRepository.deleteByEmailAndPurpose(email, purpose);
        emailVerificationRepository.flush(); // ← 추가: DELETE를 즉시 DB에 반영

        String code = RandomStringUtils.randomNumeric(6);

        emailVerificationRepository.save(
                EmailVerification.builder()
                        .email(email)
                        .code(code)
                        .purpose(purpose)
                        .verified(false)
                        .build()
        );

        sendEmail(email, code, purpose);
    }

    /**
     * 인증번호 검증 (상태 저장)
     */
    @Transactional
    public void verifyCode(String email, String code, EmailVerification.Purpose purpose) {

        EmailVerification verification = emailVerificationRepository
                .findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.BAD_REQUEST, "인증번호를 먼저 요청해주세요."));

        if (verification.isVerified()) {
            return;
        }

        checkCode(verification, code);

        checkExpiry(verification);

        verification.verify();
        emailVerificationRepository.save(verification);
    }

    /**
     * 최종 검증 + 삭제 (회원가입 / 비밀번호 변경)
     */
    @Transactional
    public void verifyAndDelete(String email, String code, EmailVerification.Purpose purpose) {

        EmailVerification verification = emailVerificationRepository
                .findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."));

        if (!verification.isVerified()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "이메일 인증을 먼저 완료해주세요."
            );
        }

        checkCode(verification, code);
        checkExpiry(verification);

        emailVerificationRepository.delete(verification);
    }

    // ================= 내부 메서드 =================

    private void checkExpiry(EmailVerification verification) {
        if (verification.getCreatedAt()
                .plusMinutes(EXPIRY_MINUTES)
                .isBefore(LocalDateTime.now())) {

            throw new CustomException(
                    HttpStatus.GONE,
                    "인증번호가 만료되었습니다."
            );
        }
    }

    private void checkCode(EmailVerification verification, String code) {
        if (!verification.getCode().equals(code)) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "인증번호가 올바르지 않습니다."
            );
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
            throw new CustomException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "이메일 전송에 실패했습니다."
            );
        }
    }
}