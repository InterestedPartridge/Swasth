package com.swasth.swasth.service;

import com.swasth.swasth.dto.*;
import com.swasth.swasth.entities.*;
import com.swasth.swasth.repositories.*;
import com.swasth.swasth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepo;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Transactional
    public FamilyResponse createFamily(CreateFamilyRequest dto, String callerEmail) {
        // check if user already has a family
        User caller = userRepo.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // fetch patient corresponding to the user
        Patient patient = patientRepo.findByAccountHolder(caller)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found for user"));

        if (patient.getFamily() != null) {
            throw new IllegalArgumentException("User already belongs to a family");
        }

        // create family
        Family family = Family.builder()
                .familyName(dto.getFamilyName())
                .inviteCode(RandomStringUtils.randomAlphanumeric(8).toUpperCase())
                .build();

        // save family
        familyRepo.save(family);

        // set family for patient
        patient.setFamily(family);
        patientRepo.save(patient);

        // return response
        return new FamilyResponse(family.getFamilyName(),
                family.getInviteCode(),
                List.of(toResponse(patient)));
    }

    @Transactional
    public FamilyResponse joinFamily(String inviteCode, String callerEmail) {
        // check if user is present
        User caller = userRepo.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // fetch patient corresponding to the user
        Patient patient = patientRepo.findByAccountHolder(caller)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found for user"));

        if (patient.getFamily() != null) {
            throw new IllegalArgumentException("User already belongs to a family");
        }

        // fetch family by invite code
        Family family = familyRepo.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        // add patient to family
        patient.setFamily(family);
        patientRepo.save(patient);

        // return response
        return buildFamilyResponse(family);
    }

    // Patient
    public List<PatientResponse> listFamilyMembers(String callerEmail) {
        Patient p = patientRepo.findByAccountHolder(userRepo.findByEmail(callerEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Caller not found")))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found for user"));
        Family f = p.getFamily();
        List<Patient> patients = patientRepo.findByFamily(f);
        return patients.stream().map(this::toResponse).toList();
    }

    /* helpers */
    private AuthResponse buildTokens(User user) {
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(details))
                .refreshToken(jwtUtil.generateRefreshToken(details))
                .build();
    }

    public FamilyResponse buildFamilyResponse(Family f) {
        List<PatientResponse> patients = f.getPatients()
                .stream().map(this::toResponse).toList();
        return FamilyResponse.builder()
                .familyName(f.getFamilyName())
                .inviteCode(f.getInviteCode())
                .patients(patients)
                .build();
    }

    private PatientResponse toResponse(Patient p) {
        return new PatientResponse(p.getId(), p.getFullName(), p.getDateOfBirth(),
                p.getGender(), p.getPhone(),
                p.getFamily().getInviteCode());
    }

}