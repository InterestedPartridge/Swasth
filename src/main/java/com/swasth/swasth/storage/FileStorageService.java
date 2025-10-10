package com.swasth.swasth.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String directory) throws IOException;
    Resource loadFileAsResource(String storedFilename, String directory) throws IOException;
    void deleteFile(String storedFilename, String directory) throws IOException;
}