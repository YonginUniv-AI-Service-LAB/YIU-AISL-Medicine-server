package com.aisl.mediroutine.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class IntakeItemResponse {

    private Long intakeId;
    private Long medicineId;
    private String medicineName;
    private String category;

    private LocalTime scheduledTime;
    private String status;
    private Integer dayOfWeek;

}