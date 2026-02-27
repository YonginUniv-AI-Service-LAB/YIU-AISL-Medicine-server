package com.aisl.mediroutine.dto.response;

import lombok.Getter;

@Getter
public class EmailVerifyResponse {

    private final boolean verified;

    private EmailVerifyResponse(boolean verified) {
        this.verified = verified;
    }

    public static EmailVerifyResponse success() {
        return new EmailVerifyResponse(true);
    }
}