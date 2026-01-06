package com.swasth.swasth.controller;

import com.swasth.swasth.dto.*;
import com.swasth.swasth.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    /* ---------- CREATE ---------- */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicationResponse create(
            @Valid @RequestBody MedicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return medicationService.create(request, userDetails.getUsername());
    }

    /* ---------- UPDATE ---------- */
    @PutMapping("/{medId}")
    public MedicationResponse update(
            @PathVariable Long medId,
            @Valid @RequestBody MedicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return medicationService.update(medId, request, userDetails.getUsername());
    }

    /* ---------- SOFT STOP ---------- */
    @PatchMapping("/{medId}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stop(
            @PathVariable Long medId,
            @AuthenticationPrincipal UserDetails userDetails) {
        medicationService.stop(medId, userDetails.getUsername());
    }

    /* ---------- DELETE ---------- */
    @DeleteMapping("/{medId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long medId,
            @AuthenticationPrincipal UserDetails userDetails) {
        medicationService.delete(medId, userDetails.getUsername());
    }

    /* ---------- SINGLE ---------- */
    @GetMapping("/{medId}")
    public MedicationResponse getSingle(
            @PathVariable Long medId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return medicationService.getSingle(medId, userDetails.getUsername());
    }

    /* ---------- LIST FOR PATIENT ---------- */
    @GetMapping("/patients/{patientId}")
    public List<MedicationResponse> listForPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @AuthenticationPrincipal UserDetails userDetails) {
        return medicationService.listForPatient(patientId, userDetails.getUsername(), date, activeOnly);
    }

    /* ---------- MARK DOSE TAKEN ---------- */
    @PostMapping("/{medId}/doses")
    @ResponseStatus(HttpStatus.CREATED)
    public DoseLogResponse markDose(
            @PathVariable Long medId,
            @Valid @RequestBody DoseLogRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return medicationService.markDose(medId, request, userDetails.getUsername());
    }

    /* ---------- DOSE HISTORY ---------- */
    @GetMapping("/{medId}/doses")
    public List<DoseLogResponse> doseHistory(
            @PathVariable Long medId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails) {

        // ←  zero logic, just pass nullable dates
        return medicationService.doseHistory(medId, userDetails.getUsername(), from, to);
    }

    /* ---------- REMINDER FEED (cron / internal) ---------- */
    @GetMapping("/reminders")
    public List<MedicationResponse> reminders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        /* restrict to ROLE_NURSE / ROLE_SYSTEM if you wish */
        return medicationService.remindersFor(date == null ? LocalDate.now() : date);
    }

    /* ---------- Get Current Reminder for Poll ---------- */
    @GetMapping("/due-now")
    public List<MedicationResponse> getDueReminders(
            @RequestParam Long patientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return medicationService.getDueReminders(patientId, userDetails.getUsername());
    }
}
