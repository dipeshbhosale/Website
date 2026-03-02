package com.skillnest.calendar.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CalendarService {

    private static final String COLLECTION = "events";

    private Firestore db() { return FirestoreClient.getFirestore(); }

    // ── GET ALL EVENTS FOR USER ────────────────────────────────
    public List<Map<String, Object>> getUserEvents(String userId)
            throws ExecutionException, InterruptedException {
        QuerySnapshot snap = db().collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("start")
                .get().get();

        return snap.getDocuments().stream()
                .map(d -> {
                    Map<String, Object> data = new HashMap<>(d.getData());
                    data.put("id", d.getId());
                    return data;
                })
                .collect(Collectors.toList());
    }

    // ── GET SINGLE EVENT ──────────────────────────────────────
    public Map<String, Object> getEvent(String eventId, String userId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db().collection(COLLECTION).document(eventId).get().get();
        if (!doc.exists()) return null;
        Map<String, Object> data = doc.getData();
        // Ensure user can only see their own events
        if (!userId.equals(data.get("userId"))) return null;
        data = new HashMap<>(data);
        data.put("id", doc.getId());
        return data;
    }

    // ── CREATE ────────────────────────────────────────────────
    public Map<String, Object> createEvent(Map<String, Object> payload, String userId)
            throws ExecutionException, InterruptedException {
        payload.put("userId",    userId);
        payload.put("createdAt", Instant.now().toString());

        DocumentReference ref = db().collection(COLLECTION).document();
        ref.set(payload).get();
        payload.put("id", ref.getId());
        log.info("✅ Event created | id: {} | user: {}", ref.getId(), userId);
        return payload;
    }

    // ── UPDATE ────────────────────────────────────────────────
    public Map<String, Object> updateEvent(String eventId, Map<String, Object> updates, String userId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot existing = db().collection(COLLECTION).document(eventId).get().get();
        if (!existing.exists()) return null;
        if (!userId.equals(existing.getData().get("userId"))) {
            throw new SecurityException("Cannot modify another user's event.");
        }
        updates.put("updatedAt", Instant.now().toString());
        db().collection(COLLECTION).document(eventId).update(updates).get();
        return getEvent(eventId, userId);
    }

    // ── DELETE ────────────────────────────────────────────────
    public boolean deleteEvent(String eventId, String userId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot existing = db().collection(COLLECTION).document(eventId).get().get();
        if (!existing.exists()) return false;
        if (!userId.equals(existing.getData().get("userId"))) {
            throw new SecurityException("Cannot delete another user's event.");
        }
        db().collection(COLLECTION).document(eventId).delete().get();
        log.info("🗑 Event deleted | id: {} | user: {}", eventId, userId);
        return true;
    }
}
