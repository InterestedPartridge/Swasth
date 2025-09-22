package com.swasth.swasth.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Family {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String inviteCode;

    @Column(nullable = false)
    private String familyName;
}