package com.skillnest.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRecord {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
    private String role;            // "student" | "teacher"
    private Instant createdAt;
    private Map<String, Object> customClaims;
}
