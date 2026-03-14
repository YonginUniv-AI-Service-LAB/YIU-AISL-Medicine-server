package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.MedicineIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MedicineIntakeRepository extends JpaRepository<MedicineIntake, Long> {

    List<MedicineIntake> findByUserIdAndIntakeDate(Long userId, LocalDate intakeDate);

    @Query("""
        SELECT i
        FROM MedicineIntake i
        JOIN FETCH i.medicine m
        WHERE i.user.id = :userId
          AND i.intakeDate = :date
    """)
    List<MedicineIntake> findByUserAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    Optional<MedicineIntake> findByMedicineIdAndIntakeDateAndScheduledTime(
            Long medicineId,
            LocalDate intakeDate,
            LocalTime scheduledTime
    );
}