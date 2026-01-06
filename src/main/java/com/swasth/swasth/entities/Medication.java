package com.swasth.swasth.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String medicineName;

    // dosage instructions - e.g., "1 tablet every 8 hours"
    @Column(nullable = false)
    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Timing timing;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;          // null = open-ended

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Frequency frequency;

    /* store comma-separated integers 0-6 ; null if frequency != CUSTOM */
    private String customDays;

    @Builder.Default
    private Boolean reminderEnabled = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /* ---------- convenience helpers ---------- */
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate));
    }

    /* ---------- enums ---------- */
    public enum Timing {
        PRE_BREAKFAST, POST_BREAKFAST,
        PRE_LUNCH, POST_LUNCH,
        PRE_DINNER, POST_DINNER,
        BEDTIME
    }

    public enum Frequency {
        DAILY, WEEKLY, BI_WEEKLY, MONTHLY, CUSTOM
    }
}