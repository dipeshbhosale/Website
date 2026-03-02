package com.skillnest.enrollment.service;

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
public class EnrollmentService {

    private static final String ENROLLMENTS = "enrollments";
    private static final String USERS       = "users";
    private static final String COURSES     = "courses";

    private Firestore db() { return FirestoreClient.getFirestore(); }

    // ── ENROLL ────────────────────────────────────────────────
    public Map<String, Object> enroll(String userId, String courseId)
            throws ExecutionException, InterruptedException {

        String docId = userId + "_" + courseId;
        DocumentReference ref = db().collection(ENROLLMENTS).document(docId);

        if (ref.get().get().exists()) {
            return Map.of("message", "Already enrolled", "enrolled", true);
        }

        Map<String, Object> enrollment = new HashMap<>();
        enrollment.put("userId",       userId);
        enrollment.put("courseId",     courseId);
        enrollment.put("enrolledAt",   Instant.now().toString());
        enrollment.put("progress",     0);
        enrollment.put("completedLessons", new ArrayList<>());
        enrollment.put("completed",    false);

        ref.set(enrollment).get();

        // Also add courseId to user's enrolledCourses array
        db().collection(USERS).document(userId)
                .update("enrolledCourses", FieldValue.arrayUnion(courseId))
                .get();

        // Increment course student count
        db().collection(COURSES).document(courseId)
                .update("totalStudents", FieldValue.increment(1))
                .get();

        log.info("✅ Enrolled | user: {} | course: {}", userId, courseId);
        enrollment.put("id", docId);
        return enrollment;
    }

    // ── UNENROLL ──────────────────────────────────────────────
    public Map<String, String> unenroll(String userId, String courseId)
            throws ExecutionException, InterruptedException {

        String docId = userId + "_" + courseId;
        DocumentSnapshot doc = db().collection(ENROLLMENTS).document(docId).get().get();
        if (!doc.exists()) return Map.of("message", "Not enrolled");

        db().collection(ENROLLMENTS).document(docId).delete().get();

        db().collection(USERS).document(userId)
                .update("enrolledCourses", FieldValue.arrayRemove(courseId))
                .get();

        db().collection(COURSES).document(courseId)
                .update("totalStudents", FieldValue.increment(-1))
                .get();

        log.info("🗑 Unenrolled | user: {} | course: {}", userId, courseId);
        return Map.of("message", "Unenrolled successfully");
    }

    // ── GET USER ENROLLMENTS ──────────────────────────────────
    public List<Map<String, Object>> getUserEnrollments(String userId)
            throws ExecutionException, InterruptedException {

        QuerySnapshot snap = db().collection(ENROLLMENTS)
                .whereEqualTo("userId", userId)
                .get().get();

        return snap.getDocuments().stream()
                .map(d -> {
                    Map<String, Object> data = new HashMap<>(d.getData());
                    data.put("id", d.getId());
                    return data;
                })
                .collect(Collectors.toList());
    }

    // ── UPDATE PROGRESS ───────────────────────────────────────
    public Map<String, Object> updateProgress(String userId, String courseId,
                                              String lessonId, int progressPct)
            throws ExecutionException, InterruptedException {
        String docId = userId + "_" + courseId;
        Map<String, Object> updates = new HashMap<>();
        updates.put("progress", progressPct);
        updates.put("lastAccessedAt", Instant.now().toString());
        if (lessonId != null) {
            updates.put("completedLessons", FieldValue.arrayUnion(lessonId));
        }
        if (progressPct >= 100) {
            updates.put("completed",     true);
            updates.put("completedAt",   Instant.now().toString());
        }
        db().collection(ENROLLMENTS).document(docId).update(updates).get();
        return Map.of("progress", progressPct, "completed", progressPct >= 100);
    }
}
