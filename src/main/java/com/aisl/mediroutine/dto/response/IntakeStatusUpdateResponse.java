package com.aisl.mediroutine.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IntakeStatusUpdateResponse {

    private Long intakeId;
    private String status;

}