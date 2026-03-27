package com.aisl.mediroutine.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class MedicineDetailResponse {

    private Long id;
    private String name;
    private String category;
    private Integer dailyDoseCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private String caution;

    private List<ScheduleDto> schedules;

    @Getter
    @Builder
    public static class ScheduleDto {
        private Integer dayOfWeek;
        private LocalTime intakeTime;
    }
}