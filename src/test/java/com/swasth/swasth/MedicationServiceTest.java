package com.swasth.swasth;

import com.swasth.swasth.dto.*;
import com.swasth.swasth.entities.Medication;
import com.swasth.swasth.entities.MedicationDoseLog;
import com.swasth.swasth.entities.Patient;
import com.swasth.swasth.entities.User;
import com.swasth.swasth.repositories.MedicationDoseLogRepository;
import com.swasth.swasth.repositories.MedicationRepository;
import com.swasth.swasth.repositories.PatientRepository;
import com.swasth.swasth.repositories.UserRepository;
import com.swasth.swasth.service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock MedicationRepository medicationRepository;
    @Mock MedicationDoseLogRepository doseLogRepository;
    @Mock PatientRepository patientRepository;
    @Mock UserRepository userRepository;

    @InjectMocks MedicationService service;

    private User user;
    private Patient patient;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");

        patient = new Patient();
        patient.setId(10L);
        patient.setAccountHolder(user);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(patientRepository.findByAccountHolder(user)).thenReturn(Optional.of(patient));
    }

    private MedicationRequest baseRequest() {
        return MedicationRequest.builder()
                .patientId(patient.getId())
                .medicineName("Amoxicillin")
                .dosage("500mg")
                .timing(Medication.Timing.PRE_BREAKFAST.name())
                .startDate(LocalDate.of(2024,1,1))
                .endDate(LocalDate.of(2024,1,10))
                .frequency(Medication.Frequency.DAILY.name())
                .customDays(null)
                .reminder(null) // default true in service
                .notes("after food")
                .build();
    }

    private Medication existingMedication() {
        return Medication.builder()
                .id(100L)
                .patient(patient)
                .medicineName("Old")
                .dosage("100mg")
                .timing(Medication.Timing.POST_DINNER)
                .startDate(LocalDate.of(2023,12,1))
                .endDate(null)
                .frequency(Medication.Frequency.WEEKLY)
                .customDays(null)
                .reminderEnabled(true)
                .notes("n/a")
                .build();
    }

    @Test
    @DisplayName("create() should persist medication for caller's own patient and default reminder true when null")
    void create_persists_with_default_reminder() {
        MedicationRequest req = baseRequest();

        ArgumentCaptor<Medication> medCaptor = ArgumentCaptor.forClass(Medication.class);
        when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> {
            Medication m = inv.getArgument(0);
            m.setId(111L);
            return m;
        });

        MedicationResponse res = service.create(req, "john@example.com");

        verify(medicationRepository).save(medCaptor.capture());
        Medication saved = medCaptor.getValue();
        assertTrue(saved.getReminderEnabled());
        assertEquals(patient.getId(), saved.getPatient().getId());
        assertEquals(111L, res.getId());
        assertTrue(res.getReminder());
        assertEquals(req.getMedicineName(), res.getMedicineName());
    }

    @Test
    @DisplayName("create() should reject when patientId differs from caller's own patient")
    void create_rejects_if_patient_mismatch() {
        MedicationRequest req = baseRequest();
        req.setPatientId(999L);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> service.create(req, "john@example.com"));
        assertTrue(ex.getMessage().contains("only add medications for yourself"));
        verifyNoInteractions(medicationRepository);
    }

    @Test
    @DisplayName("update() should mutate fields on existing medication and return response")
    void update_mutates_fields() {
        Medication existing = existingMedication();
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(existing));

        MedicationRequest req = baseRequest();
        req.setMedicineName("NewMed");
        req.setDosage("250mg");
        req.setTiming(Medication.Timing.PRE_LUNCH.name());
        req.setFrequency(Medication.Frequency.CUSTOM.name());
        req.setCustomDays(List.of(1,3,5));
        req.setReminder(Boolean.FALSE);

        MedicationResponse res = service.update(100L, req, "john@example.com");

        assertEquals("NewMed", existing.getMedicineName());
        assertEquals("250mg", existing.getDosage());
        assertEquals(Medication.Timing.PRE_LUNCH, existing.getTiming());
        assertEquals(Medication.Frequency.CUSTOM, existing.getFrequency());
        assertEquals("1,3,5", existing.getCustomDays());
        assertFalse(existing.getReminderEnabled());
        assertIterableEquals(List.of(1,3,5), res.getCustomDays());
        assertFalse(res.getReminder());
    }

    @Test
    @DisplayName("stop() should set endDate to today when caller owns medication")
    void stop_sets_endDate_today() {
        Medication existing = existingMedication();
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(existing));

        service.stop(100L, "john@example.com");

        assertEquals(LocalDate.now(), existing.getEndDate());
    }

    @Test
    @DisplayName("delete() should remove medication when caller owns it")
    void delete_removes_medication() {
        Medication existing = existingMedication();
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(existing));

        service.delete(100L, "john@example.com");

        verify(medicationRepository).delete(existing);
    }

    @Test
    @DisplayName("getSingle() should fetch and map response when caller owns it")
    void getSingle_maps_response() {
        Medication existing = existingMedication();
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(existing));

        MedicationResponse res = service.getSingle(100L, "john@example.com");
        assertNull(res.getId()); // service doesn't set id unless persisted in mocks
        assertEquals(patient.getId(), res.getPatientId());
        assertEquals(existing.getMedicineName(), res.getMedicineName());
    }

    @Test
    @DisplayName("listForPatient() should enforce ownership and map responses")
    void listForPatient_enforces_and_maps() {
        Medication m1 = existingMedication();
        Medication m2 = existingMedication();
        m2.setId(200L);
        when(medicationRepository.findByPatientOrderByStartDateDesc(patient)).thenReturn(List.of(m1, m2));

        List<MedicationResponse> list = service.listForPatient(patient.getId(), "john@example.com", null, false);

        assertEquals(2, list.size());
        assertEquals(m1.getMedicineName(), list.get(0).getMedicineName());
    }

    @Test
    @DisplayName("listForPatient() with activeOnly and date should call findActiveOnDate")
    void listForPatient_activeOnly_uses_repo_query() {
        LocalDate date = LocalDate.of(2024,2,1);
        when(medicationRepository.findActiveOnDate(patient, date)).thenReturn(List.of(existingMedication()));

        List<MedicationResponse> list = service.listForPatient(patient.getId(), "john@example.com", date, true);
        assertEquals(1, list.size());
        verify(medicationRepository).findActiveOnDate(patient, date);
    }

    @Test
    @DisplayName("markDose() should create log with now when takenAt is null")
    void markDose_creates_log_with_now_when_null() {
        Medication existing = existingMedication();
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(existing));

        ArgumentCaptor<MedicationDoseLog> captor = ArgumentCaptor.forClass(MedicationDoseLog.class);
        when(doseLogRepository.save(any(MedicationDoseLog.class))).thenAnswer(inv -> inv.getArgument(0));

        DoseLogResponse res = service.markDose(100L, DoseLogRequest.builder().takenAt(null).notes("ok").build(), "john@example.com");

        verify(doseLogRepository).save(captor.capture());
        MedicationDoseLog saved = captor.getValue();
        assertSame(existing, saved.getMedication());
        assertNotNull(saved.getTakenAt());
        assertEquals(existing.getId(), res.getMedicationId());
        assertEquals("ok", res.getNotes());
    }

    @Test
    @DisplayName("doseHistory() should default range and query inclusive-exclusive window")
    void doseHistory_defaults_and_queries() {
        Medication existing = existingMedication();
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(existing));

        MedicationDoseLog l1 = MedicationDoseLog.builder().id(1L).medication(existing).takenAt(LocalDateTime.now().minusDays(1)).notes(null).build();
        when(doseLogRepository.findByMedicationAndTakenAtBetweenOrderByTakenAtDesc(any(), any(), any()))
                .thenReturn(List.of(l1));

        List<DoseLogResponse> res = service.doseHistory(100L, "john@example.com", null, null);

        assertEquals(1, res.size());
        assertEquals(existing.getId(), res.get(0).getMedicationId());
        verify(doseLogRepository).findByMedicationAndTakenAtBetweenOrderByTakenAtDesc(eq(existing), any(), any());
    }

    @Nested
    @DisplayName("Ownership validation")
    class OwnershipValidation {
        @Test
        @DisplayName("update() should throw when caller doesn't own medication")
        void update_throws_for_non_owner() {
            Medication other = existingMedication();
            other.getPatient().getAccountHolder().setEmail("alice@example.com");
            when(medicationRepository.findById(100L)).thenReturn(Optional.of(other));

            AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                    () -> service.update(100L, baseRequest(), "john@example.com"));
            assertTrue(ex.getMessage().contains("Not your medication"));
        }

        @Test
        @DisplayName("getSingle() should throw when not owner")
        void getSingle_throws_for_non_owner() {
            Medication other = existingMedication();
            other.getPatient().getAccountHolder().setEmail("alice@example.com");
            when(medicationRepository.findById(100L)).thenReturn(Optional.of(other));

            assertThrows(AccessDeniedException.class, () -> service.getSingle(100L, "john@example.com"));
        }
    }
}
