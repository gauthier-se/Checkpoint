package com.checkpoint.api.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.checkpoint.api.exceptions.InvalidFileException;
import com.checkpoint.api.services.StorageService;

/**
 * Local filesystem implementation of {@link StorageService}.
 * Stores uploaded files in a configurable directory on the local filesystem.
 */
@Service
public class LocalStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB

    private final Path rootLocation;

    /**
     * Constructs a new LocalStorageServiceImpl.
     *
     * @param uploadDir the root directory for file uploads (default: "uploads")
     */
    public LocalStorageServiceImpl(@Value("${storage.upload-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory: " + this.rootLocation, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String store(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds the maximum allowed size of 2 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException("File type not allowed. Accepted types: JPEG, PNG, WebP");
        }

        String extension = getExtension(contentType);
        String filename = UUID.randomUUID() + extension;
        String relativePath = directory + "/" + filename;

        try {
            Path targetDir = rootLocation.resolve(directory);
            Files.createDirectories(targetDir);

            Path targetFile = rootLocation.resolve(relativePath).normalize();
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file: {}", relativePath);
            return relativePath;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + relativePath, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String path) {
        if (path == null || path.isBlank()) {
            return;
        }

        try {
            Path file = rootLocation.resolve(path).normalize();
            if (Files.exists(file)) {
                Files.delete(file);
                log.info("Deleted file: {}", path);
            }
        } catch (IOException ex) {
            log.warn("Failed to delete file: {}", path, ex);
        }
    }

    /**
     * Returns the file extension for the given content type.
     *
     * @param contentType the MIME content type
     * @return the file extension including the dot
     */
    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
