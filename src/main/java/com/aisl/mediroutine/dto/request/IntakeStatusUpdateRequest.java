package com.aisl.mediroutine.dto.request;

import lombok.Getter;

@Getter
public class IntakeStatusUpdateRequest {

    private String status; // BEFORE | TAKEN | MISSED

}