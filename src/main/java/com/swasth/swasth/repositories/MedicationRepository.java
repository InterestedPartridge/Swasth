package com.swasth.swasth.repositories;

import com.swasth.swasth.entities.Medication;
import com.swasth.swasth.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    /* ----- patient-centric lists ----- */
    List<Medication> findByPatientOrderByStartDateDesc(Patient patient);

    /* ----- active on a given date ----- */
    @Query("""
        select m from Medication m
        where m.patient = :patient
          and :date between m.startDate and coalesce(m.endDate, :date)
        order by m.timing
    """)
    List<Medication> findActiveOnDate(Patient patient, LocalDate date);

    /* ----- bulk reminder query (cron job) ----- */
    @Query("""
        select m from Medication m
        where m.reminderEnabled = true
          and :today between m.startDate and coalesce(m.endDate, :today)
    """)
    List<Medication> findRemindersForToday(LocalDate today);

    @Query("""
    SELECT m FROM Medication m
    WHERE m.patient = :patient
      AND m.timing BETWEEN :startTime AND :endTime
      AND m.reminderEnabled = true
""")
    List<Medication> findDueRemindersByPatient(@Param("patient") Patient patient,
                                               @Param("startTime") LocalTime startTime,
                                               @Param("endTime") LocalTime endTime);

}