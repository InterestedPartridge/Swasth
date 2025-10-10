package com.swasth.swasth.dto;

import com.swasth.swasth.entities.Family;
import com.swasth.swasth.entities.Patient;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyResponse {
    private String familyName;
    private String inviteCode;
    private List<PatientResponse> patients;
}