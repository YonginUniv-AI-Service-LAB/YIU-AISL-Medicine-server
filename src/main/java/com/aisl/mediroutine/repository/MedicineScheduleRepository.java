package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.MedicineSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicineScheduleRepository extends JpaRepository<MedicineSchedule, Long> {

    List<MedicineSchedule> findAllByMedicineIdIn(List<Long> medicineIds);

    void deleteAllByMedicineId(Long medicineId);

    @Query("""
        SELECT ms
        FROM MedicineSchedule ms
        JOIN FETCH ms.medicine m
        WHERE m.user.id = :userId
          AND m.startDate <= :date
          AND m.endDate >= :date
          AND ms.dayOfWeek = :dayOfWeek
    """)
    List<MedicineSchedule> findSchedulesForDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("dayOfWeek") Integer dayOfWeek
    );
}