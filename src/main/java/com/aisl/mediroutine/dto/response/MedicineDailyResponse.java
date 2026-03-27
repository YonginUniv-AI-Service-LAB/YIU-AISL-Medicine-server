package com.aisl.mediroutine.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class MedicineDailyResponse {

    private Long scheduleId;
    private LocalTime scheduledTime;

    private String status;
    private LocalDateTime takenAt;

    private MedicineInfo medicine;

    @Getter
    @Builder
    public static class MedicineInfo {

        private Long id;
        private String name;
        private String category;
        private Integer dailyDoseCount;

        private LocalDate startDate;
        private LocalDate endDate;

        private List<Integer> daysOfWeek;

        private String caution;
    }
}