package com.aisl.mediroutine.dto.response;

import com.aisl.mediroutine.entity.Medicine;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class MedicineResponse {

    private Long id;
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

    public static MedicineResponse from(
            Medicine medicine,
            LocalTime intakeTime,
            String status
    ) {
        return MedicineResponse.builder()
                .id(medicine.getId())
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
                .build();
    }
}