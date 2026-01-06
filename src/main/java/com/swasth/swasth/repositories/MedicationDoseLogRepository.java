package com.swasth.swasth.repositories;

import com.swasth.swasth.entities.Medication;
import com.swasth.swasth.entities.MedicationDoseLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicationDoseLogRepository extends JpaRepository<MedicationDoseLog, Long> {

    /* ----- adherence history ----- */
    List<MedicationDoseLog> findByMedicationAndTakenAtBetweenOrderByTakenAtDesc(
            Medication medication, LocalDateTime from, LocalDateTime to);

    /* ----- single medication ----- */
    List<MedicationDoseLog> findByMedicationOrderByTakenAtDesc(Medication medication);
}