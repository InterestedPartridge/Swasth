package com.swasth.swasth.dto;
import jdk.jshell.Snippet;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionResponse {
    private Long id;
    private String fileName;
    private String fileUrl;
    private LocalDate visitDate;
    private String doctorName;
    private String clinicName;
}