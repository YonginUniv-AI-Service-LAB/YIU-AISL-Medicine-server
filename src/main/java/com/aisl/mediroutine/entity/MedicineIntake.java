package com.aisl.mediroutine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "medicine_intake")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class MedicineIntake {

    public enum Status {
        BEFORE, TAKEN, MISSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: medicine_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    // FK: user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "intake_date", nullable = false)
    private LocalDate intakeDate; // 실제 예정 복용 날짜

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime; // 예정 복용 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.BEFORE;

    @Column(name = "taken_at")
    private LocalDateTime takenAt; // 실제 복용 시각(기록)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 복용 완료 처리
    public void markTaken() {
        this.status = Status.TAKEN;
        this.takenAt = LocalDateTime.now();
    }

    // 미복용 처리
    public void markMissed() {
        this.status = Status.MISSED;
        this.takenAt = null;
    }
}