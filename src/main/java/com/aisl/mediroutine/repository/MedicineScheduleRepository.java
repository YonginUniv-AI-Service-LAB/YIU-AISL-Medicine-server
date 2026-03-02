package com.aisl.mediroutine.repository;

import com.aisl.mediroutine.entity.MedicineSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineScheduleRepository extends JpaRepository<MedicineSchedule, Long> {
    List<MedicineSchedule> findAllByMedicineIdIn(List<Long> medicineIds);
}