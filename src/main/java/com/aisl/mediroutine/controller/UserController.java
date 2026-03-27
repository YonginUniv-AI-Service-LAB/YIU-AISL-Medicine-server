package com.aisl.mediroutine.controller;

import com.aisl.mediroutine.dto.response.UserResponse;
import com.aisl.mediroutine.global.response.ApiResponse;
import com.aisl.mediroutine.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponse response = authService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }
}