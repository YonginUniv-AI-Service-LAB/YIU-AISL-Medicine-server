package com.aisl.mediroutine.controller;

import com.aisl.mediroutine.dto.request.IntakeStatusUpdateRequest;
import com.aisl.mediroutine.dto.response.IntakeDailyResponse;
import com.aisl.mediroutine.dto.response.IntakeStatusUpdateResponse;
import com.aisl.mediroutine.global.exception.CustomException;
import com.aisl.mediroutine.service.IntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/intakes")
public class IntakeController {

    private final IntakeService intakeService;

    // 특정 날짜 복용 기록 조회
    @GetMapping
    public IntakeDailyResponse getIntakesByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate date
    ) {

        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return intakeService.getIntakesByDate(
                userDetails.getUsername(),
                date
        );
    }

    // 복용 상태 변경
    @PatchMapping("/{intakeId}")
    public IntakeStatusUpdateResponse updateIntakeStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long intakeId,
            @RequestBody IntakeStatusUpdateRequest request
    ) {

        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return intakeService.updateIntakeStatus(
                userDetails.getUsername(),
                intakeId,
                request
        );
    }
}