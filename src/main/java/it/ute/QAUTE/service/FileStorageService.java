package it.ute.QAUTE.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface FileStorageService {
    String storeFile(MultipartFile file, String oldAvatar, String folderType);

    String extractExtension(String filename);

    void deleteFile(String imageUrl);

    String guessExtByContentType(String ct);
}
