package com.aisl.mediroutine.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class MedicineAllSchedulesResponse {

    private Long id;
    private String name;
    private List<ScheduleEntry> schedules;

    @Getter
    @Builder
    public static class ScheduleEntry {
        private Integer dayOfWeek;
        private LocalTime intakeTime;
    }
}
