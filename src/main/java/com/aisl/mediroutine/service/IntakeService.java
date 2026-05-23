package com.aisl.mediroutine.service;

import com.aisl.mediroutine.dto.request.IntakeStatusUpdateRequest;
import com.aisl.mediroutine.dto.response.IntakeDailyResponse;
import com.aisl.mediroutine.dto.response.IntakeItemResponse;
import com.aisl.mediroutine.dto.response.IntakeStatusUpdateResponse;
import com.aisl.mediroutine.entity.Medicine;
import com.aisl.mediroutine.entity.MedicineIntake;
import com.aisl.mediroutine.entity.User;
import com.aisl.mediroutine.global.exception.CustomException;
import com.aisl.mediroutine.repository.MedicineIntakeRepository;
import com.aisl.mediroutine.repository.MedicineScheduleRepository;
import com.aisl.mediroutine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class IntakeService {

    private final MedicineIntakeRepository medicineIntakeRepository;
    private final MedicineScheduleRepository medicineScheduleRepository;
    private final UserRepository userRepository;

    // 특정 날짜 복용 기록 조회
    @Transactional(readOnly = true)
    public IntakeDailyResponse getIntakesByDate(String email, LocalDate date) {

        // 로그인 사용자 조회
        // IllegalArgumentException → CustomException: GlobalExceptionHandler가 IllegalArgumentException을 500으로 처리하므로 404로 반환하기 위해 수정
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        int dayOfWeek = date.getDayOfWeek().getValue();
        int total = medicineScheduleRepository.findSchedulesForDate(userId, date, dayOfWeek).size();

        List<MedicineIntake> intakes =
                medicineIntakeRepository.findByUserAndDate(userId, date);

        List<IntakeItemResponse> items = intakes.stream()
                .sorted(Comparator.comparing(MedicineIntake::getScheduledTime))
                .map(i -> {

                    Medicine medicine = i.getMedicine();

                    Integer dow = medicine.getSchedules().stream()
                            .filter(s -> s.getIntakeTime().equals(i.getScheduledTime()))
                            .map(s -> s.getDayOfWeek())
                            .findFirst()
                            .orElse(null);

                    return IntakeItemResponse.builder()
                            .intakeId(i.getId())
                            .medicineId(medicine.getId())
                            .medicineName(medicine.getName())
                            .category(medicine.getCategory())
                            .scheduledTime(i.getScheduledTime())
                            .status(i.getStatus().name())
                            .dayOfWeek(dow)
                            .build();
                })
                .toList();

        int taken = (int) intakes.stream()
                .filter(i -> i.getStatus() == MedicineIntake.Status.TAKEN)
                .count();

        int missed = (int) intakes.stream()
                .filter(i -> i.getStatus() == MedicineIntake.Status.MISSED)
                .count();

        int before = total - taken - missed;

        int takenRate = total == 0 ? 0 : (taken * 100) / total;
        int missedRate = total == 0 ? 0 : (missed * 100) / total;
        int beforeRate = total == 0 ? 0 : 100 - takenRate - missedRate;

        return IntakeDailyResponse.builder()
                .date(date)
                .totalCount(total)
                .takenCount(taken)
                .missedCount(missed)
                .beforeCount(before)
                .takenRate(takenRate)
                .missedRate(missedRate)
                .beforeRate(beforeRate)
                .intakes(items)
                .build();
    }

    // 복용 상태 변경
    @Transactional
    public IntakeStatusUpdateResponse updateIntakeStatus(
            String email,
            Long intakeId,
            IntakeStatusUpdateRequest request
    ) {

        // 로그인 사용자 조회
        // IllegalArgumentException → CustomException: GlobalExceptionHandler가 IllegalArgumentException을 500으로 처리하므로 404로 반환하기 위해 수정
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // intake 조회
        MedicineIntake intake = medicineIntakeRepository.findById(intakeId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "복용 기록을 찾을 수 없습니다."));

        // 본인 확인
        if (!intake.getUser().getId().equals(user.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        Medicine medicine = intake.getMedicine();

        // IllegalArgumentException → CustomException: valueOf()가 잘못된 enum 값에 대해 IllegalArgumentException을 던지므로 400으로 반환하기 위해 수정
        MedicineIntake.Status newStatus;
        try {
            newStatus = MedicineIntake.Status.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "올바른 상태 값을 입력하세요.");
        }

        MedicineIntake.Status oldStatus = intake.getStatus();

        // remaining_quantity 변경
        if (oldStatus != newStatus) {

            if (oldStatus == MedicineIntake.Status.BEFORE &&
                    newStatus == MedicineIntake.Status.TAKEN) {

                medicine.updateRemainingQuantity(
                        medicine.getRemainingQuantity() - 1
                );

            } else if (oldStatus == MedicineIntake.Status.TAKEN &&
                    newStatus == MedicineIntake.Status.BEFORE) {

                medicine.updateRemainingQuantity(
                        medicine.getRemainingQuantity() + 1
                );

            } else if (oldStatus == MedicineIntake.Status.TAKEN &&
                    newStatus == MedicineIntake.Status.MISSED) {

                medicine.updateRemainingQuantity(
                        medicine.getRemainingQuantity() + 1
                );

            } else if (oldStatus == MedicineIntake.Status.MISSED &&
                    newStatus == MedicineIntake.Status.TAKEN) {

                medicine.updateRemainingQuantity(
                        medicine.getRemainingQuantity() - 1
                );
            }
        }

        // status 변경
        switch (newStatus) {

            case TAKEN -> intake.markTaken();

            case MISSED -> intake.markMissed();

            case BEFORE -> intake.resetToBefore();
        }

        return IntakeStatusUpdateResponse.builder()
                .intakeId(intake.getId())
                .status(intake.getStatus().name())
                .build();
    }
}