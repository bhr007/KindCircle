package com.kindcircle.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    private static final String UPLOAD_DIR = "uploads";
    private static final String POSTS_DIR = "posts";
    private static final String PROFILE_PICTURES_DIR = "profile-pictures";
    private static final String CAMPAIGN_PROOFS_DIR = "campaign-proofs";
    
    /**
     * Save a file to the uploads directory
     * @param file The file to save
     * @param subdirectory The subdirectory (e.g., "posts", "profile-pictures")
     * @return The relative path to the saved file (e.g., "uploads/posts/filename.jpg")
     */
    public String saveFile(MultipartFile file, String subdirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Create directory structure: uploads/{subdirectory}/
        Path uploadPath = Paths.get(UPLOAD_DIR, subdirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for database storage
        return UPLOAD_DIR + "/" + subdirectory + "/" + uniqueFilename;
    }
    
    /**
     * Save a post media file (image or video)
     */
    public String savePostMedia(MultipartFile file) throws IOException {
        return saveFile(file, POSTS_DIR);
    }
    
    /**
     * Save a profile picture
     */
    public String saveProfilePicture(MultipartFile file) throws IOException {
        return saveFile(file, PROFILE_PICTURES_DIR);
    }

    /**
     * Save a campaign proof (photo/video)
     */
    public String saveCampaignProof(MultipartFile file) throws IOException {
        return saveFile(file, CAMPAIGN_PROOFS_DIR);
    }
    
    /**
     * Delete a file by its path
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Silently handle file deletion errors
        }
    }
    
    /**
     * Get the full URL path for serving files
     * This will be used by the frontend to access files
     */
    public String getFileUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        // Return path that can be accessed via /uploads/** endpoint
        return "/" + filePath.replace("\\", "/");
    }
}
