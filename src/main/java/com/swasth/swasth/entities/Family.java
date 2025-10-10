package com.swasth.swasth.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Family {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String inviteCode;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Patient> patients = new ArrayList<>();

    @Column(nullable = false)
    private String familyName;
}