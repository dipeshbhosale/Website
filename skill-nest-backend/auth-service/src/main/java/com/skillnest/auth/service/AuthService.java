package com.skillnest.auth.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import com.skillnest.auth.model.UserRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class AuthService {

    private static final String USERS_COLLECTION = "users";

    /**
     * Verifies the Firebase ID token and returns decoded user info.
     */
    public UserRecord verifyToken(String idToken) throws Exception {
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);

        String role = (String) decoded.getClaims().getOrDefault("role", "student");

        return UserRecord.builder()
                .uid(decoded.getUid())
                .email(decoded.getEmail())
                .displayName(decoded.getName())
                .photoUrl(decoded.getPicture())
                .role(role)
                .customClaims(decoded.getClaims())
                .build();
    }

    /**
     * Called after a user registers via the frontend Firebase SDK.
     * Creates/updates the user document in Firestore and sets role claim.
     */
    public UserRecord registerUser(String idToken, String role) throws Exception {
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decoded.getUid();

        // Validate role
        if (!role.equals("student") && !role.equals("teacher")) {
            role = "student";
        }

        // Set custom claim on Firebase Auth
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);

        // Create Firestore user document
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(USERS_COLLECTION).document(uid);

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid",         uid);
        userData.put("email",       decoded.getEmail());
        userData.put("displayName", decoded.getName() != null ? decoded.getName() : "New User");
        userData.put("photoUrl",    decoded.getPicture());
        userData.put("role",        role);
        userData.put("createdAt",   Instant.now().toString());
        userData.put("bio",         "");
        userData.put("enrolledCourses", new java.util.ArrayList<>());

        docRef.set(userData, com.google.cloud.firestore.SetOptions.merge()).get();

        log.info("✅ User registered | uid: {} | role: {}", uid, role);

        return UserRecord.builder()
                .uid(uid)
                .email(decoded.getEmail())
                .displayName(decoded.getName())
                .role(role)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Fetches user profile from Firestore.
     */
    public Map<String, Object> getUserProfile(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        var docSnap = db.collection(USERS_COLLECTION).document(uid).get().get();
        if (docSnap.exists()) {
            return docSnap.getData();
        }
        return Map.of("error", "User not found");
    }

    /**
     * Updates a user's role both in Firebase Auth custom claims and Firestore.
     */
    public void updateRole(String uid, String newRole) throws Exception {
        if (!newRole.equals("student") && !newRole.equals("teacher")) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }
        // Update Firebase Auth custom claim
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", newRole);
        FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);

        // Update Firestore document
        Firestore db = FirestoreClient.getFirestore();
        db.collection(USERS_COLLECTION).document(uid)
                .update("role", newRole)
                .get();

        log.info("✅ Role updated | uid: {} | newRole: {}", uid, newRole);
    }
}
