package com.swasth.swasth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateFamilyRequest {
    @NotBlank private String familyName;
    @NotBlank private String firstMemberName;
}