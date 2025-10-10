package com.swasth.swasth.service;

import com.swasth.swasth.dto.PrescriptionRequest;
import com.swasth.swasth.dto.PrescriptionResponse;
import com.swasth.swasth.entities.Patient;
import com.swasth.swasth.entities.Prescription;
import com.swasth.swasth.entities.User;
import com.swasth.swasth.repositories.PatientRepository;
import com.swasth.swasth.repositories.PrescriptionRepository;
import com.swasth.swasth.repositories.UserRepository;
import com.swasth.swasth.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;
    private final FileStorageService fileStorage;

//    private final String uploadRoot = "./uploads/prescriptions";
    private final String uploadDir = "prescriptions";
//    @PostConstruct
//    public void init() throws IOException {
//        Files.createDirectories(Paths.get(uploadRoot));
//    }

    /* ---------- single-call upload (multipart + JSON) ---------- */
    @Transactional
    public PrescriptionResponse uploadPrescription(Long patientId, MultipartFile file, PrescriptionRequest meta, String callerEmail) throws IOException {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        if (!patient.getAccountHolder().getEmail().equals(callerEmail))
            throw new AccessDeniedException("You can only upload to your own profile");

        String original = file.getOriginalFilename();
        String stored = fileStorage.uploadFile(file, uploadDir);   // UUID + timestamp + ext
        String fileUrl = "/patients/" + patientId + "/prescriptions/" + stored;   // UUID URL

        Prescription p = Prescription.builder()
                .fileName(original)                       // human name
                .fileUrl(fileUrl)                         // UUID URL
                .visitDate(meta.getVisitDate())
                .doctorName(meta.getDoctorName())
                .clinicName(meta.getClinicName())
                .patient(patient)
                .build();
        return toResponse(prescriptionRepository.save(p));
    }


    public List<PrescriptionResponse> list(String callerEmail) {
        User user = userRepo.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Patient patient = patientRepo.findByAccountHolder(user)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        return prescriptionRepository.findByPatientOrderByVisitDateDesc(patient)
                .stream().map(this::toResponse).toList();
    }

    public ResponseEntity<Resource> downloadPrescription(Long presId, String callerEmail) throws IOException {

        Prescription p = prescriptionRepository.findById(presId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        User user = userRepo.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Patient patient = patientRepo.findByAccountHolder(user)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (!p.getPatient().getId().equals(patient.getId()))
            throw new IllegalArgumentException("Prescription does not belong to patient");

//        Path path = Paths.get(uploadRoot, p.getFileUrl().substring(p.getFileUrl().lastIndexOf('/') + 1));
//        Resource resource = new UrlResource(path.toUri());
//        if (!resource.exists()) throw new IllegalArgumentException("File not found");
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + p.getFileName() + "\"")
//                .body(resource);

        Resource resource = fileStorage.loadFileAsResource(
                p.getFileUrl().substring(p.getFileUrl().lastIndexOf('/') + 1),
                uploadDir);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + p.getFileName() + "\"")
                .body(resource);
    }

    private PrescriptionResponse toResponse(Prescription p) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .fileName(p.getFileName())
                .fileUrl(p.getFileUrl())
                .visitDate(p.getVisitDate())
                .doctorName(p.getDoctorName())
                .clinicName(p.getClinicName())
                .build();
    }

    public ResponseEntity<Resource> viewFile(Long presId, String callerEmail) throws IOException {

        Prescription p = prescriptionRepository.findById(presId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        User user = userRepo.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Patient patient = patientRepo.findByAccountHolder(user)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (!p.getPatient().getId().equals(patient.getId()))
            throw new IllegalArgumentException("Prescription does not belong to patient");

        String stored = p.getFileUrl().substring(p.getFileUrl().lastIndexOf('/') + 1);
        Resource resource = fileStorage.loadFileAsResource(stored, "prescriptions");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("Content-Disposition", "inline; filename=\"" + p.getFileName() + "\"")
                .body(resource);
    }
}