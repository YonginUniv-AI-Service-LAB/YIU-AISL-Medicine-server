package com.aisl.mediroutine.service;

import com.aisl.mediroutine.dto.request.MedicineCreateRequest;
import com.aisl.mediroutine.dto.response.*;
import com.aisl.mediroutine.dto.request.MedicineUpdateRequest;
import com.aisl.mediroutine.global.exception.CustomException;
import com.aisl.mediroutine.repository.FriendRepository;
import com.aisl.mediroutine.entity.Medicine;
import com.aisl.mediroutine.entity.MedicineIntake;
import com.aisl.mediroutine.entity.MedicineSchedule;
import com.aisl.mediroutine.entity.User;
import com.aisl.mediroutine.repository.MedicineIntakeRepository;
import com.aisl.mediroutine.repository.MedicineRepository;
import com.aisl.mediroutine.repository.MedicineScheduleRepository;
import com.aisl.mediroutine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineScheduleRepository medicineScheduleRepository;
    private final MedicineIntakeRepository medicineIntakeRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    // 약 목록 조회
    @Transactional(readOnly = true)
    public List<MedicineResponse> getMedicines(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );

        Long userId = user.getId();

        LocalDate today = LocalDate.now();

        // 오늘 스케줄 조회
        List<MedicineSchedule> schedules = getSchedulesForDate(userId, today);

        // 오늘 복용 기록 조회
        List<MedicineIntake> intakes =
                medicineIntakeRepository.findByUserIdAndIntakeDate(userId, today);

        Map<String, MedicineIntake> intakeMap =
                intakes.stream()
                        .collect(Collectors.toMap(
                                i -> i.getMedicine().getId() + "_" + i.getScheduledTime(),
                                i -> i
                        ));

        return schedules.stream()
                .map(schedule -> {

                    Medicine medicine = schedule.getMedicine();

                    String key = medicine.getId() + "_" + schedule.getIntakeTime();
                    MedicineIntake intake = intakeMap.get(key);

                    String status = "BEFORE";

                    if (intake != null) {
                        switch (intake.getStatus()) {
                            case TAKEN -> status = "TAKEN";
                            case MISSED -> status = "NOT_TAKEN";
                            default -> status = "BEFORE";
                        }
                    }

                    return MedicineResponse.from(
                            medicine,
                            schedule.getIntakeTime(),
                            status
                    );

                })
                .sorted(Comparator.comparing(MedicineResponse::getIntakeTime))
                .toList();
    }

    // 약 생성
    @Transactional
    public MedicineCreateResponse createMedicine(String email, MedicineCreateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(request.getDurationDays());

        Medicine medicine = Medicine.builder()
                .user(user)
                .name(request.getName())
                .category(request.getCategory())
                .dailyDoseCount(request.getDailyDoseCount())
                .startDate(startDate)
                .endDate(endDate)
                .totalQuantity(request.getTotalQuantity())
                .remainingQuantity(request.getTotalQuantity())
                .caution(request.getCaution())
                .build();

        medicineRepository.save(medicine);

        request.getSchedules().forEach(scheduleDto -> {

            MedicineSchedule schedule = MedicineSchedule.builder()
                    .medicine(medicine)
                    .dayOfWeek(scheduleDto.getDayOfWeek())
                    .intakeTime(scheduleDto.getIntakeTime())
                    .build();

            medicineScheduleRepository.save(schedule);

            LocalDate date = startDate;

            while (!date.isAfter(endDate)) {

                if (date.getDayOfWeek().getValue() == scheduleDto.getDayOfWeek()) {

                    MedicineIntake intake = MedicineIntake.builder()
                            .medicine(medicine)
                            .user(user)
                            .intakeDate(date)
                            .scheduledTime(scheduleDto.getIntakeTime())
                            .status(MedicineIntake.Status.BEFORE)
                            .build();

                    medicineIntakeRepository.save(intake);
                }

                date = date.plusDays(1);
            }
        });

        return MedicineCreateResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
    // 약 수정
    @Transactional
    public MedicineUpdateResponse updateMedicine(
            String email,
            Long medicineId,
            MedicineUpdateRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "약을 찾을 수 없습니다.")
                );

        // 소유자 확인
        if (!medicine.getUser().getId().equals(user.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        // 필드 수정
        if (request.getName() != null) {
            medicine.updateName(request.getName());
        }

        if (request.getDailyDoseCount() != null) {
            medicine.updateDailyDoseCount(request.getDailyDoseCount());
        }

        // schedules가 있을 때만 재생성
        if (request.getSchedules() != null) {

            // 기존 schedule 삭제
            medicineScheduleRepository.deleteAllByMedicineId(medicineId);

            request.getSchedules().forEach(scheduleDto -> {

                MedicineSchedule schedule = MedicineSchedule.builder()
                        .medicine(medicine)
                        .dayOfWeek(scheduleDto.getDayOfWeek())
                        .intakeTime(scheduleDto.getIntakeTime())
                        .build();

                medicineScheduleRepository.save(schedule);
            });
        }

        return MedicineUpdateResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .build();
    }
    // 약 삭제
    @Transactional
    public void deleteMedicine(String email, Long medicineId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "약을 찾을 수 없습니다.")
                );

        // 소유자 확인
        if (!medicine.getUser().getId().equals(user.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        medicineRepository.delete(medicine);
    }
    // 약 상세 조회
    @Transactional(readOnly = true)
    public MedicineDetailResponse getMedicineDetail(
            String email,
            Long medicineId
    ) {

        // 로그인 사용자 조회
        User loginUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );

        // medicine 조회
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "약을 찾을 수 없습니다.")
                );

        Long ownerId = medicine.getUser().getId();
        Long loginUserId = loginUser.getId();

        // 권한 검사 (본인 or 친구)
        if (!ownerId.equals(loginUserId)) {

            boolean isFriend =
                    friendRepository.existsAcceptedBetween(loginUserId, ownerId) == 1;

            if (!isFriend) {
                throw new CustomException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다.");
            }
        }

        // schedule 조회
        List<MedicineSchedule> schedules =
                medicineScheduleRepository.findAllByMedicineIdIn(List.of(medicineId));

        List<MedicineDetailResponse.ScheduleDto> scheduleDtos =
                schedules.stream()
                        .map(s -> MedicineDetailResponse.ScheduleDto.builder()
                                .dayOfWeek(s.getDayOfWeek())
                                .intakeTime(s.getIntakeTime())
                                .build())
                        .toList();

        // DTO 반환
        return MedicineDetailResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .category(medicine.getCategory())
                .dailyDoseCount(medicine.getDailyDoseCount())
                .startDate(medicine.getStartDate())
                .endDate(medicine.getEndDate())
                .totalQuantity(medicine.getTotalQuantity())
                .remainingQuantity(medicine.getRemainingQuantity())
                .caution(medicine.getCaution())
                .schedules(scheduleDtos)
                .build();
    }
    // 특정 날짜 약 목록 조회
    @Transactional(readOnly = true)
    public List<MedicineDailyResponse> getMedicinesByDate(String email, LocalDate date) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );

        Long userId = user.getId();

        // schedule 조회 (공통 메서드 사용)
        List<MedicineSchedule> schedules = getSchedulesForDate(userId, date);

        // intake 조회
        List<MedicineIntake> intakes =
                medicineIntakeRepository.findByUserIdAndIntakeDate(userId, date);

        Map<String, MedicineIntake> intakeMap =
                intakes.stream()
                        .collect(Collectors.toMap(
                                i -> i.getMedicine().getId() + "_" + i.getScheduledTime(),
                                i -> i
                        ));

        return schedules.stream()
                .map(schedule -> {

                    Medicine medicine = schedule.getMedicine();

                    String key = medicine.getId() + "_" + schedule.getIntakeTime();
                    MedicineIntake intake = intakeMap.get(key);

                    String status = "BEFORE";
                    LocalDateTime takenAt = null;

                    if (intake != null) {

                        switch (intake.getStatus()) {
                            case TAKEN -> status = "TAKEN";
                            case MISSED -> status = "NOT_TAKEN";
                            default -> status = "BEFORE";
                        }

                        takenAt = intake.getTakenAt();
                    }

                    return MedicineDailyResponse.builder()
                            .scheduleId(schedule.getId())
                            .scheduledTime(schedule.getIntakeTime())
                            .status(status)
                            .takenAt(takenAt)
                            .medicine(MedicineDailyResponse.MedicineInfo.builder()
                                    .id(medicine.getId())
                                    .name(medicine.getName())
                                    .category(medicine.getCategory())
                                    .dailyDoseCount(medicine.getDailyDoseCount())
                                    .startDate(medicine.getStartDate())
                                    .endDate(medicine.getEndDate())
                                    .daysOfWeek(List.of(schedule.getDayOfWeek()))
                                    .caution(medicine.getCaution())
                                    .build())
                            .build();
                })
                .sorted(Comparator.comparing(MedicineDailyResponse::getScheduledTime))
                .toList();
    }

    private List<MedicineSchedule> getSchedulesForDate(Long userId, LocalDate date) {

        int dayOfWeek = date.getDayOfWeek().getValue();

        return medicineScheduleRepository.findSchedulesForDate(
                userId,
                date,
                dayOfWeek
        );
    }
}
