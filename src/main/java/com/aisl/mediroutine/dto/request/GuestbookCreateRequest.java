package com.aisl.mediroutine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GuestbookCreateRequest {

    @NotBlank(message = "내용을 입력하세요.")
    @Size(max = 500, message = "허용된 글자 수를 초과했습니다.")
    private String content;
}