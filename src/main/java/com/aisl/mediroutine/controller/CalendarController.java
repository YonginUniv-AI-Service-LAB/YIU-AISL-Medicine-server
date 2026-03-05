package com.aisl.mediroutine.controller;

import com.aisl.mediroutine.dto.response.WeeklyCalendarResponse;
import com.aisl.mediroutine.global.response.ApiResponse;
import com.aisl.mediroutine.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/{userId}/calendar/weekly")
    public ResponseEntity<ApiResponse<WeeklyCalendarResponse>> getWeeklyCalendar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId,
            @RequestParam(required = false) String date
    ) {
        WeeklyCalendarResponse response =
                calendarService.getWeeklyCalendar(userDetails.getUsername(), userId, date);

        return ResponseEntity.ok(ApiResponse.success("주간 캘린더 조회 성공", response));
    }
}