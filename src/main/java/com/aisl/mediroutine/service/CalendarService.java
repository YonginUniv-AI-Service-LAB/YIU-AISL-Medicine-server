package com.aisl.mediroutine.service;

import com.aisl.mediroutine.dto.response.WeeklyCalendarResponse;
import com.aisl.mediroutine.entity.Medicine;
import com.aisl.mediroutine.entity.MedicineSchedule;
import com.aisl.mediroutine.entity.User;
import com.aisl.mediroutine.global.exception.CustomException;
import com.aisl.mediroutine.repository.MedicineRepository;
import com.aisl.mediroutine.repository.MedicineScheduleRepository;
import com.aisl.mediroutine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineScheduleRepository medicineScheduleRepository;
    private final FriendService friendService;

    @Transactional(readOnly = true)
    public WeeklyCalendarResponse getWeeklyCalendar(String loginEmail, Long targetUserId, String dateParam) {

        User me = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!me.getId().equals(target.getId())) {
            boolean isFriend = friendService.isFriendAccepted(me.getId(), target.getId());
            if (!isFriend) {
                throw new CustomException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다.");
            }
        }

        if (dateParam == null || dateParam.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "날짜를 입력하세요.");
        }

        LocalDate baseDate;
        try {
            baseDate = LocalDate.parse(dateParam);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "날짜 형식은 yyyy-MM-dd 입니다.");
        }

        LocalDate weekStart = baseDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = baseDate.with(DayOfWeek.SUNDAY);

        List<Medicine> medicines = medicineRepository.findAllActiveInWeekRange(target.getId(), weekStart, weekEnd);
        List<Long> medicineIds = medicines.stream().map(Medicine::getId).toList();

        List<MedicineSchedule> schedules = medicineIds.isEmpty()
                ? List.of()
                : medicineScheduleRepository.findAllByMedicineIdIn(medicineIds);

        Map<Long, Map<Integer, List<String>>> timeMap = new HashMap<>();
        for (MedicineSchedule s : schedules) {
            timeMap
                    .computeIfAbsent(s.getMedicine().getId(), k -> new HashMap<>())
                    .computeIfAbsent(s.getDayOfWeek(), k -> new ArrayList<>())
                    .add(s.getIntakeTime().toString());
        }
        timeMap.values().forEach(dayMap -> dayMap.values().forEach(Collections::sort));

        List<WeeklyCalendarResponse.CalendarDayResponse> calendarDays = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            int dow = d.getDayOfWeek().getValue();

            List<WeeklyCalendarResponse.CalendarMedicineResponse> medsForDay = new ArrayList<>();

            for (Medicine m : medicines) {
                if (d.isBefore(m.getStartDate()) || d.isAfter(m.getEndDate())) continue;

                List<String> times = Optional.ofNullable(timeMap.get(m.getId()))
                        .map(mm -> mm.get(dow))
                        .orElse(List.of());

                if (!times.isEmpty()) {
                    medsForDay.add(
                            WeeklyCalendarResponse.CalendarMedicineResponse.builder()
                                    .medicineId(m.getId())
                                    .name(m.getName())
                                    .intakeTimes(times)
                                    .build()
                    );
                }
            }

            calendarDays.add(
                    WeeklyCalendarResponse.CalendarDayResponse.builder()
                            .date(d)
                            .medicines(medsForDay)
                            .build()
            );
        }

        return WeeklyCalendarResponse.builder()
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .calendar(calendarDays)
                .build();
    }
}