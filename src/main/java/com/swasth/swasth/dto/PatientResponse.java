package com.swasth.swasth.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String familyInviteCode;
}