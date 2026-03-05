package com.aisl.mediroutine.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FriendCreateRequest {
    @NotNull(message = "userId를 입력하세요.")
    private Long userId;
}