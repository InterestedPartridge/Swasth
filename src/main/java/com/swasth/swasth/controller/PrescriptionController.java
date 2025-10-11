package com.swasth.swasth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swasth.swasth.dto.PrescriptionRequest;
import com.swasth.swasth.dto.PrescriptionResponse;
import com.swasth.swasth.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.DataInput;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/patients/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /* 1. upload file – multipart */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PrescriptionResponse uploadPrescription(
            @RequestPart("file") MultipartFile file,
            @RequestParam("patientId") Long patientId,
            @RequestParam("clinicName") String clinicName,
            @RequestParam("doctorName") String doctorName,
            @RequestParam("visitDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            Principal principal
    ) throws IOException {
        PrescriptionRequest dto = new PrescriptionRequest(patientId, visitDate, doctorName, clinicName);
        return prescriptionService.uploadPrescription(patientId, file, dto, principal.getName());
    }

    @GetMapping("/getAll")
    public List<PrescriptionResponse> getAll(Principal principal) {
        return prescriptionService.list(principal.getName());
    }

    @GetMapping("/{prescriptionId}/view")
    @Operation(summary = "View a prescription file by ID")
    public ResponseEntity<Resource> view(
            @Parameter(
                    name = "prescriptionId",
                    description = "ID of the prescription you want to view",
                    required = true,
                    example = "1"
            )
            @PathVariable("prescriptionId") Long prescriptionId,
            Principal principal
    ) throws IOException {
        return prescriptionService.viewFile(prescriptionId, principal.getName());
    }


    @GetMapping("/{prescriptionId}/download")
    public ResponseEntity<Resource> downloadPrescription(@PathVariable Long prescriptionId,
                                             Principal principal) throws IOException {
        return prescriptionService.downloadPrescription(prescriptionId, principal.getName());
    }

}
