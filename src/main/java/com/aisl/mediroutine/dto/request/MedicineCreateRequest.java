package com.aisl.mediroutine.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Getter
public class MedicineCreateRequest {

    @NotBlank(message = "약 이름을 입력하세요.")
    private String name;

    private String category;

    @Min(value = 1, message = "복용 횟수는 1 이상이어야 합니다.")
    private Integer dailyDoseCount;

    @NotNull(message = "복용 기간을 입력하세요.")
    @Min(value = 1, message = "durationDays는 1 이상이어야 합니다.")
    private Integer durationDays;

    private Integer totalQuantity;

    private String caution;

    @NotEmpty(message = "복용 시간을 설정하세요.")
    private List<ScheduleDto> schedules;

    @Getter
    public static class ScheduleDto {

        @NotNull(message = "요일을 입력하세요.")
        private Integer dayOfWeek;

        @NotNull(message = "복용 시간을 입력하세요.")
        private LocalTime intakeTime;

    }
}