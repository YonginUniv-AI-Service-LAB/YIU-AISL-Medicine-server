package com.aisl.mediroutine.controller;

import com.aisl.mediroutine.global.exception.CustomException;
import jakarta.validation.Valid;
import com.aisl.mediroutine.dto.response.*;
import com.aisl.mediroutine.dto.request.MedicineCreateRequest;
import com.aisl.mediroutine.dto.request.MedicineUpdateRequest;
import com.aisl.mediroutine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    // 약 전체 조회
    @GetMapping
    public List<MedicineResponse> getMedicines(
            @AuthenticationPrincipal UserDetails userDetails) {
        return medicineService.getMedicines(userDetails.getUsername());
    }

    // 약 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineCreateResponse createMedicine(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MedicineCreateRequest request
    ) {

        return medicineService.createMedicine(
                userDetails.getUsername(),
                request
        );
    }

    // 약 수정
    @PatchMapping("/{medicineId}")
    public MedicineUpdateResponse updateMedicine(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long medicineId,
            @RequestBody MedicineUpdateRequest request
    ) {

        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return medicineService.updateMedicine(
                userDetails.getUsername(),
                medicineId,
                request
        );
    }

    // 약 삭제
    @DeleteMapping("/{medicineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedicine(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long medicineId
    ) {

        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        medicineService.deleteMedicine(
                userDetails.getUsername(),
                medicineId
        );
    }
    // 약 상세조회
    @GetMapping("/{medicineId}")
    public MedicineDetailResponse getMedicineDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long medicineId
    ) {

        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return medicineService.getMedicineDetail(
                userDetails.getUsername(),
                medicineId
        );
    }
    // 특정 날짜 약 목록 조회
    @GetMapping("/daily")
    public List<MedicineDailyResponse> getMedicinesByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {

        if (userDetails == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return medicineService.getMedicinesByDate(
                userDetails.getUsername(),
                date
        );
    }
}