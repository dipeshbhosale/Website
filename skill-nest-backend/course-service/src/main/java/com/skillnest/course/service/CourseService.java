package com.skillnest.course.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.skillnest.course.model.Course;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseService {

    private static final String COLLECTION = "courses";

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    // ── READ ALL ──────────────────────────────────────────────
    public List<Map<String, Object>> getAllCourses(String category, String level, boolean freeOnly)
            throws ExecutionException, InterruptedException {

        CollectionReference col = db().collection(COLLECTION);
        Query query = col;

        if (category != null && !category.isBlank()) {
            query = query.whereEqualTo("category", category);
        }
        if (level != null && !level.isBlank()) {
            query = query.whereEqualTo("level", level);
        }
        if (freeOnly) {
            query = query.whereEqualTo("isFree", true);
        }

        QuerySnapshot snapshot = query.get().get();
        return snapshot.getDocuments().stream()
                .map(d -> {
                    Map<String, Object> data = new HashMap<>(d.getData());
                    data.put("id", d.getId());
                    return data;
                })
                .collect(Collectors.toList());
    }

    // ── READ ONE ──────────────────────────────────────────────
    public Map<String, Object> getCourseById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db().collection(COLLECTION).document(id).get().get();
        if (!doc.exists()) return null;
        Map<String, Object> data = new HashMap<>(doc.getData());
        data.put("id", doc.getId());
        return data;
    }

    // ── FEATURED ──────────────────────────────────────────────
    public List<Map<String, Object>> getFeaturedCourses() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = db().collection(COLLECTION)
                .whereEqualTo("isFeatured", true)
                .limit(8)
                .get().get();

        return snapshot.getDocuments().stream()
                .map(d -> {
                    Map<String, Object> data = new HashMap<>(d.getData());
                    data.put("id", d.getId());
                    return data;
                })
                .collect(Collectors.toList());
    }

    // ── CREATE ────────────────────────────────────────────────
    public Map<String, Object> createCourse(Map<String, Object> payload, String instructorId)
            throws ExecutionException, InterruptedException {

        payload.put("instructorId", instructorId);
        payload.put("createdAt",    Instant.now().toString());
        payload.put("updatedAt",    Instant.now().toString());
        payload.put("totalStudents", 0);
        payload.put("rating",       0.0);

        DocumentReference docRef = db().collection(COLLECTION).document();
        docRef.set(payload).get();

        payload.put("id", docRef.getId());
        log.info("✅ Course created | id: {}", docRef.getId());
        return payload;
    }

    // ── UPDATE ────────────────────────────────────────────────
    public Map<String, Object> updateCourse(String id, Map<String, Object> updates, String callerUid)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot existing = db().collection(COLLECTION).document(id).get().get();
        if (!existing.exists()) return null;

        String ownerId = (String) existing.getData().get("instructorId");
        if (!callerUid.equals(ownerId)) {
            throw new SecurityException("Only the course instructor can update this course.");
        }

        updates.put("updatedAt", Instant.now().toString());
        db().collection(COLLECTION).document(id).update(updates).get();
        return getCourseById(id);
    }

    // ── DELETE ────────────────────────────────────────────────
    public boolean deleteCourse(String id, String callerUid)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot existing = db().collection(COLLECTION).document(id).get().get();
        if (!existing.exists()) return false;

        String ownerId = (String) existing.getData().get("instructorId");
        if (!callerUid.equals(ownerId)) {
            throw new SecurityException("Only the course instructor can delete this course.");
        }

        db().collection(COLLECTION).document(id).delete().get();
        log.info("🗑 Course deleted | id: {}", id);
        return true;
    }

    // ── RATE ──────────────────────────────────────────────────
    public Map<String, Object> rateCourse(String courseId, String userId, double rating)
            throws ExecutionException, InterruptedException {

        // Store rating in subcollection ratings/{userId}
        Map<String, Object> ratingDoc = new HashMap<>();
        ratingDoc.put("userId", userId);
        ratingDoc.put("rating", rating);
        ratingDoc.put("createdAt", Instant.now().toString());

        db().collection(COLLECTION).document(courseId)
                .collection("ratings").document(userId)
                .set(ratingDoc).get();

        // Recalculate average
        QuerySnapshot allRatings = db().collection(COLLECTION).document(courseId)
                .collection("ratings").get().get();

        double avg = allRatings.getDocuments().stream()
                .mapToDouble(d -> ((Number) d.getData().get("rating")).doubleValue())
                .average().orElse(0.0);

        // Round to 1 decimal
        avg = Math.round(avg * 10.0) / 10.0;

        db().collection(COLLECTION).document(courseId).update("rating", avg).get();
        return getCourseById(courseId);
    }
}
