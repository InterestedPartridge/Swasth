package com.swasth.swasth.repositories;

import com.swasth.swasth.entities.Family;
import com.swasth.swasth.entities.Patient;
import com.swasth.swasth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByFamily(Family family);
    Optional<Patient> findByAccountHolder(User user);
}