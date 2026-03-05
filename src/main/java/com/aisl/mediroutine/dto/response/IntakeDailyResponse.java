package com.aisl.mediroutine.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class IntakeDailyResponse {

    private LocalDate date;

    private int totalCount;

    private int takenCount;
    private int missedCount;
    private int beforeCount;

    private int takenRate;
    private int missedRate;
    private int beforeRate;

    private List<IntakeItemResponse> intakes;

}