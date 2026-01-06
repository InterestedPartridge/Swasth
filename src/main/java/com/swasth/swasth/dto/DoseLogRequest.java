package com.swasth.swasth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A request object for logging medication doses.
 * This class uses Lombok annotations to reduce boilerplate code.
 * It provides automatic generation of getters, setters, no-args constructor,
 * all-args constructor, and a builder pattern implementation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoseLogRequest {

    /* accept ISO-8601 with optional millis and offset */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
    private LocalDateTime takenAt; // null = now

    private String notes;
}