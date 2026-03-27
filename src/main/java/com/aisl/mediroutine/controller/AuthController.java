package com.aisl.mediroutine.controller;

// =====================================================
// [담당 API]
//   POST   /auth/signup         → 회원가입           201 Created
//   POST   /auth/login          → 로그인             200 OK
//   POST   /auth/logout         → 로그아웃           204 No Content
//   POST   /auth/password/reset → 비밀번호 재설정    200 OK
//   DELETE /auth/user           → 회원 탈퇴          200 OK
// =====================================================

import com.aisl.mediroutine.dto.request.LoginRequest;
import com.aisl.mediroutine.dto.request.PasswordResetRequest;
import com.aisl.mediroutine.dto.request.SignupRequest;
import com.aisl.mediroutine.dto.response.LoginResponse;
import com.aisl.mediroutine.dto.response.UserResponse;
import com.aisl.mediroutine.entity.User;
import com.aisl.mediroutine.global.response.ApiResponse;
import com.aisl.mediroutine.service.AuthService;
import com.aisl.mediroutine.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CustomUserDetailsService customUserDetailsService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @RequestBody @Valid SignupRequest request
    ) {
        UserResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpSession session
    ) {
        User user = authService.login(request);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return ResponseEntity.ok(ApiResponse.success("로그인 성공", LoginResponse.from(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody @Valid PasswordResetRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다."));
    }

    @DeleteMapping("/user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session
    ) {
        authService.deleteUser(userDetails.getUsername());
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }

}