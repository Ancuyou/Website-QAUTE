package it.ute.QAUTE.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String oldAvatar, int accountID);

    String extractExtension(String filename);

    void deleteFile(String imageUrl);

    String guessExtByContentType(String ct);
}
