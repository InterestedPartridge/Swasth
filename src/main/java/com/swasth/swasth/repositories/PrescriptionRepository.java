package com.swasth.swasth.repositories;

import com.swasth.swasth.entities.Patient;
import com.swasth.swasth.entities.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientOrderByVisitDateDesc(Patient patient);
    @Query("select p from Prescription p where p.patient.id = :patientId and p.patient.accountHolder.email = :email order by p.id desc")
    Optional<Prescription> findTopByPatientIdAndAccountHolderEmailOrderByIdDesc(@Param("patientId") Long patientId, @Param("email") String email);
}
