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

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepo;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /* on-boarder creates family + his patient row */
    public AuthResponse createFirstMember(CreateFamilyRequest dto, String creatorEmail) {
        User me = userRepo.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Family family = Family.builder()
                .inviteCode(RandomStringUtils.randomAlphanumeric(8).toUpperCase())
                .familyName(dto.getFamilyName())
                .build();
        familyRepo.save(family);

        Patient patient = Patient.builder()
                .fullName(dto.getFirstMemberName())
                .family(family)
                .accountHolder(me)
                .build();
        patientRepo.save(patient);

        return buildTokens(me);
    }
    /* any adult joins with invite code → own User + Patient */
    @Transactional
    public AuthResponse joinFamily(JoinFamilyRequest dto) {
        Family family = familyRepo.findByInviteCode(dto.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        Patient newPatient = Patient.builder()
                .fullName(dto.getFullName())
                .family(family)
                .accountHolder(newUser)
                .build();
        patientRepo.save(newPatient);

        return buildTokens(newUser);
    }

    /* every member sees whole family */
    public List<PatientResponse> listFamily(String readerEmail) {
        User me = userRepo.findByEmail(readerEmail).orElseThrow();
        Patient myProfile = patientRepo.findByAccountHolder(me)
                .orElseThrow(() -> new IllegalArgumentException("No profile"));
        return patientRepo.findByFamily(myProfile.getFamily())
                .stream().map(this::toResponse)
                .toList();
    }

    /* edit only own profile */
    @Transactional
    public PatientResponse updateMyProfile(Long patientId,
                                           UpdateProfileRequest dto,
                                           String editorEmail) {
        Patient p = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (!p.getAccountHolder().getEmail().equals(editorEmail))
            throw new AccessDeniedException("You can only edit your own profile");

        p.setFullName(dto.getFullName());
        p.setDateOfBirth(dto.getDateOfBirth());
        p.setGender(dto.getGender());
        p.setPhone(dto.getPhone());
        return toResponse(patientRepo.save(p));
    }

    /* helpers */
    private AuthResponse buildTokens(User user) {
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(details))
                .refreshToken(jwtUtil.generateRefreshToken(details))
                .build();
    }

    private PatientResponse toResponse(Patient p) {
        return new PatientResponse(p.getId(), p.getFullName(), p.getDateOfBirth(),
                p.getGender(), p.getPhone(),
                p.getFamily().getInviteCode());
    }
}