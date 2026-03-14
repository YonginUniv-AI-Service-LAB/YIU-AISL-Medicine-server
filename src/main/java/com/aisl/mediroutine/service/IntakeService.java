package com.aisl.mediroutine.service;

import com.aisl.mediroutine.dto.request.IntakeStatusUpdateRequest;
import com.aisl.mediroutine.dto.response.IntakeDailyResponse;
import com.aisl.mediroutine.dto.response.IntakeItemResponse;
import com.aisl.mediroutine.dto.response.IntakeStatusUpdateResponse;
import com.aisl.mediroutine.entity.Medicine;
import com.aisl.mediroutine.entity.MedicineIntake;
import com.aisl.mediroutine.entity.User;
import com.aisl.mediroutine.repository.MedicineIntakeRepository;
import com.aisl.mediroutine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntakeService {

    private final MedicineIntakeRepository medicineIntakeRepository;
    private final UserRepository userRepository;

    // 특정 날짜 복용 기록 조회
    @Transactional(readOnly = true)
    public IntakeDailyResponse getIntakesByDate(String email, LocalDate date) {

        // 로그인 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        List<MedicineIntake> intakes =
                medicineIntakeRepository.findByUserAndDate(userId, date);

        List<IntakeItemResponse> items = intakes.stream()
                .sorted(Comparator.comparing(MedicineIntake::getScheduledTime))
                .map(i -> IntakeItemResponse.builder()
                        .intakeId(i.getId())
                        .medicineId(i.getMedicine().getId())
                        .medicineName(i.getMedicine().getName())
                        .category(i.getMedicine().getCategory())
                        .scheduledTime(i.getScheduledTime())
                        .status(i.getStatus().name())
                        .build())
                .toList();

        int total = items.size();

        int taken = (int) intakes.stream()
                .filter(i -> i.getStatus() == MedicineIntake.Status.TAKEN)
                .count();

        int missed = (int) intakes.stream()
                .filter(i -> i.getStatus() == MedicineIntake.Status.MISSED)
                .count();

        int before = total - taken - missed;

        int takenRate = total == 0 ? 0 : (taken * 100) / total;
        int missedRate = total == 0 ? 0 : (missed * 100) / total;
        int beforeRate = total == 0 ? 0 : (before * 100) / total;

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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // intake 조회
        MedicineIntake intake = medicineIntakeRepository.findById(intakeId)
                .orElseThrow(() -> new IllegalArgumentException("복용 기록을 찾을 수 없습니다."));

        // 본인 확인
        if (!intake.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        Medicine medicine = intake.getMedicine();

        MedicineIntake.Status newStatus =
                MedicineIntake.Status.valueOf(request.getStatus());

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