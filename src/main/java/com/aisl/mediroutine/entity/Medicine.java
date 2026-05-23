package com.aisl.mediroutine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicine")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String category;

    @Column(name = "daily_dose_count", nullable = false)
    private Integer dailyDoseCount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @Lob
    private String caution;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // schedule 관계
    @Builder.Default
    @OneToMany(mappedBy = "medicine",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<MedicineSchedule> schedules = new ArrayList<>();


    // intake 관계
    @Builder.Default
    @OneToMany(mappedBy = "medicine",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<MedicineIntake> intakes = new ArrayList<>();


    public void updateName(String name) {
        this.name = name;
    }

    public void updateDailyDoseCount(Integer dailyDoseCount) {
        this.dailyDoseCount = dailyDoseCount;
    }

    public void updateRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
}