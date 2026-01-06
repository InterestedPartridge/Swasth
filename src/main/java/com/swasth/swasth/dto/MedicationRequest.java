package com.swasth.swasth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationRequest {

    @NotNull
    private Long patientId;

    @NotBlank
    private String medicineName;

    @NotBlank
    private String dosage;

    @NotNull
    private String timing;   // PRE_BREAKFAST etc.

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull
    private String frequency; // DAILY, WEEKLY, BI_WEEKLY, MONTHLY, CUSTOM

    // SAME HERE
    private List<Integer> customDays;   // integers 0-6

    private Boolean reminder; // defaults true in service

    private String notes;
}