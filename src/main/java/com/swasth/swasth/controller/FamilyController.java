package com.swasth.swasth.controller;

import com.swasth.swasth.dto.*;
import com.swasth.swasth.entities.Family;
import com.swasth.swasth.repositories.PatientRepository;
import com.swasth.swasth.service.FamilyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping("/create")
    public FamilyResponse create(@Valid @RequestBody CreateFamilyRequest dto,
                                 Principal principal) {
        return familyService.createFamily(dto, principal.getName());
    }
    @GetMapping("/members")
    public List<PatientResponse> list(Principal principal) {
        return familyService.listFamilyMembers(principal.getName());
    }

    @PostMapping("/join")
    public FamilyResponse joinFamily(@RequestParam String inviteCode,
                                     Principal principal) {
        return familyService.joinFamily(inviteCode, principal.getName());
    }

//    @PutMapping("/profile/{patientId}")
//    public PatientResponse updateProfile(@PathVariable Long patientId,
//                                         @Valid @RequestBody UpdateProfileRequest dto,
//                                         Principal principal) {
//        return familyService.updateMyProfile(patientId, dto, principal.getName());
//    }
}