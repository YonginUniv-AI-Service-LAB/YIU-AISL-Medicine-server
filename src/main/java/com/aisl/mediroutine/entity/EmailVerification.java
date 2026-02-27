package com.aisl.mediroutine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    // Enum: Purpose.SIGNUP 또는 Purpose.RESET_PASSWORD 두 값만 허용
    @Enumerated(EnumType.STRING)             // DB에 "SIGNUP", "RESET_PASSWORD" 문자열로 저장
    @Column(nullable = false, length = 20)
    private Purpose purpose;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // JPA가 DB에 저장하기 직전 자동 호출 → 생성 시각 세팅
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 인증 목적을 나타내는 열거형(Enum)
    public enum Purpose {
        SIGNUP,         // 회원가입용
        RESET_PASSWORD  // 비밀번호 재설정용
    }

    // 재발송 시 코드와 시각을 갱신하는 비즈니스 메서드
    public void refresh(String newCode) {
        this.code = newCode;
        this.createdAt = LocalDateTime.now();
    }
}