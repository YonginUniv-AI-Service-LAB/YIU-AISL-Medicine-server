package com.aisl.mediroutine.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MedicineCreateResponse {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

}