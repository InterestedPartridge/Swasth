package com.swasth.swasth.repositories;

import com.swasth.swasth.entities.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByInviteCode(String code);
}