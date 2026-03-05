package com.aisl.mediroutine.dto.response;

import com.aisl.mediroutine.entity.Medicine;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MedicineResponse {

    private Long id;
    private String name;
    private String category;
    private Integer dailyDoseCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalQuantity;
    private Integer remainingQuantity;

    public static MedicineResponse from(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .category(medicine.getCategory())
                .dailyDoseCount(medicine.getDailyDoseCount())
                .startDate(medicine.getStartDate())
                .endDate(medicine.getEndDate())
                .totalQuantity(medicine.getTotalQuantity())
                .remainingQuantity(medicine.getRemainingQuantity())
                .build();
    }
}