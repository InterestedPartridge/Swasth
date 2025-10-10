package com.swasth.swasth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class PrescriptionRequest {
    private LocalDate visitDate;
    private String doctorName;
    private String clinicName;
}