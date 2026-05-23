package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.MedicineSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicineScheduleRepository extends JpaRepository<MedicineSchedule, Long> {

    @Query("SELECT ms FROM MedicineSchedule ms JOIN FETCH ms.medicine WHERE ms.medicine.id IN :medicineIds")
    List<MedicineSchedule> findAllByMedicineIdIn(@Param("medicineIds") List<Long> medicineIds);

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

    // 회원탈퇴 시 해당 유저 약의 스케줄 전체 삭제 (Medicine FK 제약 해제용)
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MedicineSchedule s WHERE s.medicine.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}