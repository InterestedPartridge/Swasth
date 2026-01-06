package com.swasth.swasth.service;

import com.swasth.swasth.dto.*;
import com.swasth.swasth.entities.*;
import com.swasth.swasth.repositories.MedicationDoseLogRepository;
import com.swasth.swasth.repositories.MedicationRepository;
import com.swasth.swasth.repositories.PatientRepository;
import com.swasth.swasth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicationDoseLogRepository doseLogRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    /* ---------- create ---------- */
    @Transactional
    public MedicationResponse create(MedicationRequest dto, String callerEmail) {
        Patient patient = getPatientByEmail(callerEmail);
        if (!patient.getId().equals(dto.getPatientId()))
            throw new AccessDeniedException("You can only add medications for yourself");

        Medication med = Medication.builder()
                .patient(patient)
                .medicineName(dto.getMedicineName())
                .dosage(dto.getDosage())
                .timing(Medication.Timing.valueOf(dto.getTiming()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .frequency(Medication.Frequency.valueOf(dto.getFrequency()))
                .customDays(joinInts(dto.getCustomDays()))
                .reminderEnabled(dto.getReminder() == null ? Boolean.TRUE : dto.getReminder())
                .notes(dto.getNotes())
                .build();

        medicationRepository.save(med);
        return toResponse(med);
    }

    /* ---------- update ---------- */
    @Transactional
    public MedicationResponse update(Long medId, MedicationRequest dto, String callerEmail) {
        Medication med = medicationRepository.findById(medId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
        validateOwnership(med, callerEmail);
        med.setMedicineName(dto.getMedicineName());
        med.setDosage(dto.getDosage());
        med.setTiming(Medication.Timing.valueOf(dto.getTiming()));
        med.setStartDate(dto.getStartDate());
        med.setEndDate(dto.getEndDate());
        med.setFrequency(Medication.Frequency.valueOf(dto.getFrequency()));
        med.setCustomDays(joinInts(dto.getCustomDays()));
        med.setReminderEnabled(dto.getReminder() == null ? Boolean.TRUE : dto.getReminder());
        med.setNotes(dto.getNotes());

        return toResponse(med);
    }

    /* ---------- soft stop ---------- */
    @Transactional
    public void stop(Long medId, String callerEmail) {
        Medication med = medicationRepository.findById(medId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
        validateOwnership(med, callerEmail);
        med.setEndDate(LocalDate.now());
    }

    /* ---------- delete ---------- */
    @Transactional
    public void delete(Long medId, String callerEmail) {
        Medication med = medicationRepository.findById(medId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
        validateOwnership(med, callerEmail);
        medicationRepository.delete(med);
    }

    /* ---------- single ---------- */
    public MedicationResponse getSingle(Long medId, String callerEmail) {
        Medication med = medicationRepository.findById(medId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
        validateOwnership(med, callerEmail);
        return toResponse(med);
    }

    /* ---------- list ---------- */
    public List<MedicationResponse> listForPatient(Long patientId, String callerEmail,
                                                   LocalDate date, boolean activeOnly) {
        Patient patient = getPatientByEmail(callerEmail);
        if (!patient.getId().equals(patientId))
            throw new AccessDeniedException("You can only list your own medications");

        List<Medication> meds = activeOnly && date != null
                ? medicationRepository.findActiveOnDate(patient, date)
                : medicationRepository.findByPatientOrderByStartDateDesc(patient);

        return meds.stream().map(this::toResponse).toList();
    }

    /* ---------- mark dose ---------- */
    @Transactional
    public DoseLogResponse markDose(Long medId, DoseLogRequest dto, String callerEmail) {
        Medication med = medicationRepository.findById(medId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
        validateOwnership(med, callerEmail);

        MedicationDoseLog log = MedicationDoseLog.builder()
                .medication(med)
                .takenAt(dto.getTakenAt() == null ? LocalDateTime.now() : dto.getTakenAt())
                .notes(dto.getNotes())
                .build();
        doseLogRepository.save(log);
        return toResponse(log);
    }

    /* ---------- dose history ---------- */
    public List<DoseLogResponse> doseHistory(Long medId, String callerEmail,
                                             LocalDate from, LocalDate to) {
        Medication med = medicationRepository.findById(medId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
        validateOwnership(med, callerEmail);

        /* ---------- business defaults ---------- */
        if (from == null) from = LocalDate.now().minusMonths(3);
        if (to == null)   to = LocalDate.now();

        /* ---------- inclusive whole-day range ---------- */
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toTime   = to.plusDays(1).atStartOfDay(); // exclusive upper bound

        return doseLogRepository.findByMedicationAndTakenAtBetweenOrderByTakenAtDesc(
                        med, fromTime, toTime)
                .stream().map(this::toResponse).toList();
    }

    /* ---------- Due Reminders for Polling (Individual Patient) ---------- */
    public List<MedicationResponse> getDueReminders(Long patientId, String callerEmail) {
        Patient callerPatient = getPatientByEmail(callerEmail);

        // 2. Calculate Time Window (Now - 10 mins to Now + 1 min)
        LocalTime now = LocalTime.now();
        LocalTime windowStart = now.minusMinutes(10);
        LocalTime windowEnd = now.plusMinutes(1);

        // 3. Fetch from Repository for THIS patient only
        List<Medication> medications = medicationRepository.findDueRemindersByPatient(
                callerPatient,
                windowStart,
                windowEnd
        );

        // 4. Convert to existing DTO
        return medications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /* ---------- reminder feed ---------- */
    public List<MedicationResponse> remindersFor(LocalDate date) {
        return medicationRepository.findRemindersForToday(date)
                .stream().map(this::toResponse).toList();
    }

    /* ---------- helpers ---------- */
    private Patient getPatientByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return patientRepository.findByAccountHolder(user)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }

    private void validateOwnership(Medication med, String email) {
        if (!med.getPatient().getAccountHolder().getEmail().equals(email))
            throw new AccessDeniedException("Not your medication");
    }

    /* ---------- tiny helpers ---------- */
    private static String joinInts(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static List<Integer> splitInts(String csv) {
        if (csv == null || csv.isBlank()) return null;
        return Arrays.stream(csv.split(",")).map(Integer::valueOf).toList();
    }

    /* ---------- manual mappers ---------- */
    private MedicationResponse toResponse(Medication med) {
        return MedicationResponse.builder()
                .id(med.getId())
                .patientId(med.getPatient().getId())
                .medicineName(med.getMedicineName())
                .dosage(med.getDosage())
                .timing(med.getTiming().name())
                .startDate(med.getStartDate())
                .endDate(med.getEndDate())
                .frequency(med.getFrequency().name())
                .customDays(splitInts(med.getCustomDays()))
                .reminder(med.getReminderEnabled())
                .notes(med.getNotes())
                .createdAt(med.getCreatedAt())
                .updatedAt(med.getUpdatedAt())
                .build();
    }

    private DoseLogResponse toResponse(MedicationDoseLog log) {
        return DoseLogResponse.builder()
                .id(log.getId())
                .medicationId(log.getMedication().getId())
                .takenAt(log.getTakenAt())
                .notes(log.getNotes())
                .build();
    }
}