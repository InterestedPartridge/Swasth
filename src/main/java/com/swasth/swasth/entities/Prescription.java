package com.swasth.swasth.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prescription {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String fileName;          // original name

    @Column(nullable = false)
    private String fileUrl;           // relative URL

    @Column(nullable = false)
    private String clinicName;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private String doctorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
}