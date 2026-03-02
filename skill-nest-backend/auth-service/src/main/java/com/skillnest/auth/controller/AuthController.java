package com.skillnest.auth.controller;

import com.skillnest.auth.model.UserRecord;
import com.skillnest.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/verify-token
     * Validates a Firebase ID token and returns decoded user info.
     * Used by the frontend after login to confirm auth state.
     */
    @PostMapping("/verify-token")
    public ResponseEntity<UserRecord> verifyToken(@RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("idToken");
            if (idToken == null || idToken.isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            UserRecord user = authService.verifyToken(idToken);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.warn("Token verification failed: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * POST /api/auth/register
     * Called after the frontend Firebase SDK creates the user.
     * Sets the role custom claim and creates the Firestore user document.
     */
    @PostMapping("/register")
    public ResponseEntity<UserRecord> register(@RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("idToken");
            String role    = body.getOrDefault("role", "student");
            UserRecord user = authService.registerUser(idToken, role);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("User registration failed: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/auth/me
     * Returns the currently authenticated user's profile from Firestore.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            Map<String, Object> profile = authService.getUserProfile(uid);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Failed to fetch user profile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * PUT /api/auth/role
     * Updates role custom claim (admin use or initial setup).
     */
    @PutMapping("/role")
    public ResponseEntity<Map<String, String>> updateRole(@RequestBody Map<String, String> body,
                                                          HttpServletRequest request) {
        try {
            String callerUid = (String) request.getAttribute("uid");
            String targetUid = body.get("uid");
            String newRole   = body.get("role");

            if (targetUid == null || newRole == null) {
                return ResponseEntity.badRequest().build();
            }
            // Only allow self-update or future admin check
            if (!callerUid.equals(targetUid)) {
                return ResponseEntity.status(403).build();
            }

            authService.updateRole(targetUid, newRole);
            return ResponseEntity.ok(Map.of("message", "Role updated to " + newRole));
        } catch (Exception e) {
            log.error("Role update failed: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
