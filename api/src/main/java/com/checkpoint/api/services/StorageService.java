package com.checkpoint.api.services;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service for storing and retrieving uploaded files.
 * Implementations may use local filesystem, S3, or other storage backends.
 */
public interface StorageService {

    /**
     * Stores a file and returns the relative path for retrieval.
     *
     * @param file      the uploaded file
     * @param directory the subdirectory to store the file in (e.g., "profiles")
     * @return the relative storage path (e.g., "profiles/uuid.jpg")
     */
    String store(MultipartFile file, String directory);

    /**
     * Deletes a previously stored file.
     *
     * @param path the relative storage path
     */
    void delete(String path);
}
