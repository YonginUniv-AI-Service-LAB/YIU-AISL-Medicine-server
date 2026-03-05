package com.aisl.mediroutine.dto.request;

import lombok.Getter;
import java.util.List;

@Getter
public class MedicineUpdateRequest {

    private String name;
    private Integer dailyDoseCount;
    private List<MedicineCreateRequest.ScheduleDto> schedules;
}