package com.aisl.mediroutine.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FriendUpdateRequest {
    @NotBlank(message = "올바른 상태 값을 입력하세요.")
    private String status; // ACCEPTED / REJECTED
}