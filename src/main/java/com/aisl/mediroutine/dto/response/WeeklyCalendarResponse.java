package com.aisl.mediroutine.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WeeklyCalendarResponse {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<CalendarDayResponse> calendar;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CalendarDayResponse {
        private LocalDate date;
        private List<CalendarMedicineResponse> medicines;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CalendarMedicineResponse {
        private Long medicineId;
        private String name;
        private List<String> intakeTimes;
    }
}