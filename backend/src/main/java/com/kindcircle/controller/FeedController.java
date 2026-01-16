package com.kindcircle.controller;

import com.kindcircle.dto.CommentRequest;
import com.kindcircle.dto.CommentResponse;
import com.kindcircle.dto.PostRequest;
import com.kindcircle.dto.PostResponse;
import com.kindcircle.service.CommentService;
import com.kindcircle.service.PostService;
import com.kindcircle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@CrossOrigin(origins = "http://localhost:3000")
public class FeedController {
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<PostResponse>> getFeed(
            @RequestParam(defaultValue = "recent") String sort) {
        List<PostResponse> posts = postService.getApprovedPosts(sort);
        return ResponseEntity.ok(posts);
    }
    
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PostResponse> createPost(
            @RequestPart("post") @Valid PostRequest request,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles) {
        try {
            PostResponse post = postService.createPost(request, mediaFiles);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid activity type: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create post: " + e.getMessage());
        }
    }
    
    @PostMapping("/{postId}/vote")
    public ResponseEntity<String> votePost(
            @PathVariable Long postId,
            @RequestBody VoteRequest request) {
        postService.votePost(postId, request.getVoteType());
        return ResponseEntity.ok("Vote recorded");
    }
    
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId, currentUserId));
    }
    
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(postId, request));
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully");
    }
    
    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<String> voteComment(
            @PathVariable Long commentId,
            @RequestBody VoteRequest request) {
        commentService.voteComment(commentId, request.getVoteType());
        return ResponseEntity.ok("Vote recorded");
    }
    
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
                return null;
            }
            String username = authentication.getName();
            return userService.findByUsername(username).getId();
        } catch (Exception e) {
            return null;
        }
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok("Post deleted successfully");
    }
    
    // Inner class for vote request
    public static class VoteRequest {
        private String voteType;
        
        public String getVoteType() { return voteType; }
        public void setVoteType(String voteType) { this.voteType = voteType; }
    }
}
