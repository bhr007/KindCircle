package com.kindcircle.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kindcircle.dto.PostResponse;
import com.kindcircle.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private PostService postService;
    
    @GetMapping("/pending")
    public ResponseEntity<List<PostResponse>> getPendingVerifications() {
        List<PostResponse> posts = postService.getPendingPosts();
        return ResponseEntity.ok(posts);
    }
    
    @PostMapping("/verify/{postId}")
    public ResponseEntity<PostResponse> verifyPost(
            @PathVariable Long postId,
            @RequestBody VerificationRequest request) {
        try {
            PostResponse post = postService.verifyPost(
                postId, 
                request.isApproved(), 
                request.getRejectionReason()
            );
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify post: " + e.getMessage(), e);
        }
    }
    
    // Inner class for verification request
    public static class VerificationRequest {
        @JsonProperty("approved")
        private boolean approved = false;
        
        @JsonProperty("rejectionReason")
        private String rejectionReason;
        
        public boolean isApproved() { 
            return approved; 
        }
        
        public void setApproved(boolean approved) { 
            this.approved = approved; 
        }
        
        public String getRejectionReason() { 
            return rejectionReason; 
        }
        
        public void setRejectionReason(String rejectionReason) { 
            this.rejectionReason = rejectionReason; 
        }
    }
}
