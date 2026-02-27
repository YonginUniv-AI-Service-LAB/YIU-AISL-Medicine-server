package com.aisl.mediroutine.controller;

// =====================================================
// [담당 API]
//   POST /emails/verification-code/send   → 인증번호 발송
//   POST /emails/verification-code/verify → 인증번호 검증
// =====================================================

import com.aisl.mediroutine.dto.request.EmailSendRequest;
import com.aisl.mediroutine.dto.request.EmailVerifyRequest;
import com.aisl.mediroutine.dto.response.EmailVerifyResponse;
import com.aisl.mediroutine.global.response.ApiResponse;
import com.aisl.mediroutine.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/verification-code/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @RequestBody @Valid EmailSendRequest request
    ) {
        emailService.sendVerificationCode(request.getEmail(), request.getPurpose());
        return ResponseEntity.ok(
                ApiResponse.success("인증번호가 이메일로 발송되었습니다."));
    }

    @PostMapping("/verification-code/verify")
    public ResponseEntity<ApiResponse<EmailVerifyResponse>> verifyCode(
            @RequestBody @Valid EmailVerifyRequest request
    ) {
        emailService.verifyCode(request.getEmail(), request.getCode(), request.getPurpose());
        return ResponseEntity.ok(
                ApiResponse.success("인증번호 확인이 완료되었습니다.", EmailVerifyResponse.success()));
    }
}