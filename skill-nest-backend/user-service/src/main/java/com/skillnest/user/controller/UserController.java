package com.skillnest.user.controller;

import com.skillnest.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/me — own full profile
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            Map<String, Object> profile = userService.getProfile(uid);
            return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // PUT /api/users/me — update own profile
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMe(@RequestBody Map<String, Object> body,
                                                         HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(userService.updateProfile(uid, body));
        } catch (Exception e) {
            log.error("Update profile failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/users/{uid} — public profile
    @GetMapping("/{uid}")
    public ResponseEntity<Map<String, Object>> getPublicProfile(@PathVariable String uid) {
        try {
            Map<String, Object> profile = userService.getPublicProfile(uid);
            return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/users/search?q=name
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam String q) {
        try {
            return ResponseEntity.ok(userService.searchUsers(q));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
