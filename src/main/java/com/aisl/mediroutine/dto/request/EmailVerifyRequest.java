package com.aisl.mediroutine.dto.request;


import com.aisl.mediroutine.entity.EmailVerification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EmailVerifyRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotNull(message = "purpose는 필수 입력값입니다.")
    private EmailVerification.Purpose purpose;

    @NotBlank(message = "인증번호를 입력해주세요.")
    private String code;
}