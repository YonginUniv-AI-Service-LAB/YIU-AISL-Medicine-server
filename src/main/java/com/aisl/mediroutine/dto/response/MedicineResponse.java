package com.aisl.mediroutine.dto.response;

import com.aisl.mediroutine.entity.Medicine;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class MedicineResponse {

    private Long id;
    private Long intakeId;
    private String name;
    private String category;

    private LocalTime intakeTime;
    private String status; // TAKEN / MISSED / PENDING

    private Integer dailyDoseCount;
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer totalQuantity;
    private Integer remainingQuantity;

    private String caution;

    // 캘린더 표시를 위한 전체 요일별 스케줄 목록 (dayOfWeek: 1=월…7=일)
    private List<ScheduleInfo> schedules;

    @Getter
    @Builder
    public static class ScheduleInfo {
        private Long id;
        private Integer dayOfWeek;
    }

    public static MedicineResponse from(
            Medicine medicine,
            LocalTime intakeTime,
            String status,
            List<ScheduleInfo> schedules,
            Long intakeId
    ) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .intakeId(intakeId)
                .name(medicine.getName())
                .category(medicine.getCategory())
                .intakeTime(intakeTime)
                .status(status)
                .dailyDoseCount(medicine.getDailyDoseCount())
                .startDate(medicine.getStartDate())
                .endDate(medicine.getEndDate())
                .totalQuantity(medicine.getTotalQuantity())
                .remainingQuantity(medicine.getRemainingQuantity())
                .caution(medicine.getCaution())
                .schedules(schedules)
                .build();
    }
}