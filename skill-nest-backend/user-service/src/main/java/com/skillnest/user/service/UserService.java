package com.skillnest.user.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class UserService {

    private static final String COLLECTION = "users";

    private Firestore db() { return FirestoreClient.getFirestore(); }

    public Map<String, Object> getProfile(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db().collection(COLLECTION).document(uid).get().get();
        if (!doc.exists()) return null;
        Map<String, Object> data = new HashMap<>(doc.getData());
        data.put("uid", doc.getId());
        return data;
    }

    public Map<String, Object> updateProfile(String uid, Map<String, Object> updates)
            throws ExecutionException, InterruptedException {
        // Whitelist allowed fields
        Set<String> allowed = Set.of("displayName","bio","website","twitter","linkedin",
                                     "language","timezone","photoUrl");
        Map<String, Object> safe = new HashMap<>();
        updates.forEach((k, v) -> { if (allowed.contains(k)) safe.put(k, v); });
        safe.put("updatedAt", Instant.now().toString());

        db().collection(COLLECTION).document(uid).update(safe).get();
        log.info("✅ Profile updated | uid: {}", uid);
        return getProfile(uid);
    }

    public List<Map<String, Object>> searchUsers(String query)
            throws ExecutionException, InterruptedException {
        // Simple prefix search on displayName
        QuerySnapshot snap = db().collection(COLLECTION)
                .orderBy("displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get().get();

        List<Map<String, Object>> results = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = new HashMap<>(doc.getData());
            data.put("uid", doc.getId());
            // Remove sensitive fields
            data.remove("enrolledCourses");
            results.add(data);
        }
        return results;
    }

    public Map<String, Object> getPublicProfile(String uid)
            throws ExecutionException, InterruptedException {
        Map<String, Object> full = getProfile(uid);
        if (full == null) return null;
        // Return only public fields
        Map<String, Object> pub = new HashMap<>();
        List.of("uid","displayName","photoUrl","bio","role","website","twitter","linkedin")
                .forEach(k -> { if (full.containsKey(k)) pub.put(k, full.get(k)); });
        return pub;
    }
}
