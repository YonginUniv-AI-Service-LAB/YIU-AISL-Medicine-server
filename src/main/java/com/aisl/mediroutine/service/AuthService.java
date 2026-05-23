package com.aisl.mediroutine.service;

// =====================================================
// [담당 API]
//   POST  /auth/signup         → 인증번호 재검증 + 회원가입
//   POST  /auth/login          → 이메일/비밀번호 검증
//   PATCH /auth/password/reset → 인증번호 재검증 + 비밀번호 변경
//   DELETE /auth/user          → 회원 탈퇴
// =====================================================

import com.aisl.mediroutine.dto.request.LoginRequest;
import com.aisl.mediroutine.dto.request.PasswordResetRequest;
import com.aisl.mediroutine.dto.request.SignupRequest;
import com.aisl.mediroutine.dto.response.UserResponse;
import com.aisl.mediroutine.entity.EmailVerification;
import com.aisl.mediroutine.entity.User;
import com.aisl.mediroutine.global.exception.CustomException;
import com.aisl.mediroutine.repository.EmailVerificationRepository;
import com.aisl.mediroutine.repository.FriendRepository;
import com.aisl.mediroutine.repository.GuestbookRepository;
import com.aisl.mediroutine.repository.MedicineIntakeRepository;
import com.aisl.mediroutine.repository.MedicineRepository;
import com.aisl.mediroutine.repository.MedicineScheduleRepository;
import com.aisl.mediroutine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MedicineIntakeRepository medicineIntakeRepository;
    private final MedicineScheduleRepository medicineScheduleRepository;
    private final MedicineRepository medicineRepository;
    private final FriendRepository friendRepository;
    private final GuestbookRepository guestbookRepository;


    @Transactional
    public UserResponse signup(SignupRequest request) {

        emailService.verifyAndDelete(
                request.getEmail(),
                request.getCode(),
                EmailVerification.Purpose.SIGNUP
        );


        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }


        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        return UserResponse.from(userRepository.save(user));
    }


    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다.");
        }

        return user;
    }


    @Transactional
    public void resetPassword(PasswordResetRequest request) {

        emailService.verifyAndDelete(
                request.getEmail(),
                request.getCode(),
                EmailVerification.Purpose.RESET_PASSWORD
        );


        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다."));


        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }


    @Transactional
    public void deleteUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        // FK 제약 순서대로 자식 테이블 먼저 삭제해야 User 삭제 가능
        medicineIntakeRepository.deleteByUserId(userId);       // medicine_intake.user_id FK
        medicineScheduleRepository.deleteAllByUserId(userId);  // medicine_schedule.medicine_id → medicine.user_id FK
        medicineRepository.deleteByUserId(userId);             // medicine.user_id FK
        friendRepository.deleteAllByUserId(userId);            // friend.requester_id / receiver_id FK
        guestbookRepository.deleteAllByUserId(userId);         // guestbook.owner_id / writer_id FK
        emailVerificationRepository.deleteByEmail(email);

        userRepository.delete(user);
    }

    /**
     * GET /users/me
     * 현재 로그인한 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }
}