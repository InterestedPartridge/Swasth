package com.swasth.swasth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class JoinFamilyRequest {
    @NotBlank private String inviteCode;
    @NotBlank private String fullName;
    @NotBlank @Email private String email;
}