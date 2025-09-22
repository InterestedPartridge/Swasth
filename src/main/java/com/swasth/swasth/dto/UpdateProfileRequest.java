package com.swasth.swasth.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {
    @NotBlank private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
}