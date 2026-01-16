package com.kindcircle.controller;

import com.kindcircle.dto.PostResponse;
import com.kindcircle.model.User;
import com.kindcircle.service.FileStorageService;
import com.kindcircle.service.PostService;
import com.kindcircle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private PostService postService;
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = getCurrentUserFromAuth();
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}/posts")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable Long id) {
        List<PostResponse> posts = postService.getPostsByUserId(id);
        return ResponseEntity.ok(posts);
    }
    
    @PutMapping("/me/profile")
    public ResponseEntity<User> updateProfile(@RequestBody ProfileUpdateRequest request) {
        User currentUser = getCurrentUserFromAuth();
        
        if (request.getFullName() != null) {
            currentUser.setFullName(request.getFullName());
        }
        if (request.getBloodGroup() != null) {
            currentUser.setBloodGroup(request.getBloodGroup());
        }
        if (request.getCity() != null) {
            currentUser.setCity(request.getCity());
        }
        if (request.getLocation() != null) {
            currentUser.setLocation(request.getLocation());
        }
        
        User updatedUser = userService.save(currentUser);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping(value = "/me/profile-picture", consumes = {"multipart/form-data"})
    public ResponseEntity<User> updateProfilePicture(@RequestPart("file") MultipartFile file) {
        User currentUser = getCurrentUserFromAuth();
        
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Profile photo file is required");
        }
        
        try {
            // Delete old profile picture if exists
            if (currentUser.getProfilePhotoUrl() != null && !currentUser.getProfilePhotoUrl().isEmpty()) {
                fileStorageService.deleteFile(currentUser.getProfilePhotoUrl());
            }
            
            // Save new profile picture
            String filePath = fileStorageService.saveProfilePicture(file);
            currentUser.setProfilePhotoUrl(filePath);
            
            User updatedUser = userService.save(currentUser);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload profile picture: " + e.getMessage());
        }
    }
    
    private User getCurrentUserFromAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
    
    // Inner classes for requests
    public static class ProfileUpdateRequest {
        private String fullName;
        private String bloodGroup;
        private String city;
        private String location;
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getBloodGroup() { return bloodGroup; }
        public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
    
    public static class ProfilePictureRequest {
        private String profilePhotoUrl;
        
        public String getProfilePhotoUrl() { return profilePhotoUrl; }
        public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    }
}
