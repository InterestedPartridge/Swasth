package com.swasth.swasth.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_holder_id", nullable = false)
    private User accountHolder;   // login that owns this profile
}