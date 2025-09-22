package com.swasth.swasth.controller;

import com.swasth.swasth.dto.*;
import com.swasth.swasth.service.FamilyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping("/create-first")
    public AuthResponse createFirst(@Valid @RequestBody CreateFamilyRequest dto,
                                    Principal principal) {
        return familyService.createFirstMember(dto, principal.getName());
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse join(@Valid @RequestBody JoinFamilyRequest dto) {
        return familyService.joinFamily(dto);
    }

    @GetMapping("/members")
    public List<PatientResponse> list(Principal principal) {
        return familyService.listFamily(principal.getName());
    }

    @PutMapping("/profile/{patientId}")
    public PatientResponse updateProfile(@PathVariable Long patientId,
                                         @Valid @RequestBody UpdateProfileRequest dto,
                                         Principal principal) {
        return familyService.updateMyProfile(patientId, dto, principal.getName());
    }
}