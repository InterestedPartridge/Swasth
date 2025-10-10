package com.swasth.swasth.repositories;

import com.swasth.swasth.entities.Family;
import com.swasth.swasth.entities.Patient;
import com.swasth.swasth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByInviteCode(String code);
}