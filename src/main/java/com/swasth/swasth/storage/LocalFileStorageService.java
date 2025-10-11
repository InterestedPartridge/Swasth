package com.swasth.swasth.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String baseUploadDir;

    @Override
    public String uploadFile(MultipartFile file, String directory, Long patientId, Long prescriptionId) throws IOException {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
        String stored = patientId + "_" + prescriptionId + ext;
        Path targetDir = Path.of(baseUploadDir, directory);
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(stored);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        return stored;   // only the file name (UUID + timestamp + ext)
    }

    @Override
    public Resource loadFileAsResource(String storedFilename, String directory) throws IOException {
        Path filePath = Path.of(baseUploadDir, directory, storedFilename);
        File file = filePath.toFile();
        if (!file.exists()) throw new IllegalArgumentException("File not found: " + storedFilename);
        return new FileSystemResource(file);
    }

    @Override
    public void deleteFile(String storedFilename, String directory) throws IOException {
        Path filePath = Path.of(baseUploadDir, directory, storedFilename);
        Files.deleteIfExists(filePath);
    }
}