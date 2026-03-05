package com.aisl.mediroutine.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicineUpdateResponse {

    private Long id;
    private String name;

}