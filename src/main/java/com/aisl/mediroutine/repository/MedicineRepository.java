package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByUserId(Long userId);

    @Query("""
        SELECT m
        FROM Medicine m
        WHERE m.user.id = :userId
          AND m.startDate <= :weekEnd
          AND m.endDate >= :weekStart
    """)
    List<Medicine> findAllActiveInWeekRange(@Param("userId") Long userId,
                                            @Param("weekStart") LocalDate weekStart,
                                            @Param("weekEnd") LocalDate weekEnd);
}