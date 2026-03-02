package com.skillnest.enrollment.controller;

import com.skillnest.enrollment.service.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // GET /api/enrollments — get all enrollments for the current user
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMyEnrollments(HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(enrollmentService.getUserEnrollments(uid));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST /api/enrollments/{courseId} — enroll in a course
    @PostMapping("/{courseId}")
    public ResponseEntity<Map<String, Object>> enroll(@PathVariable String courseId,
                                                       HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(enrollmentService.enroll(uid, courseId));
        } catch (Exception e) {
            log.error("Enroll failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // DELETE /api/enrollments/{courseId} — unenroll from a course
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Map<String, String>> unenroll(@PathVariable String courseId,
                                                         HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(enrollmentService.unenroll(uid, courseId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // PATCH /api/enrollments/{courseId}/progress — update lesson progress
    @PatchMapping("/{courseId}/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(@PathVariable String courseId,
                                                               @RequestBody Map<String, Object> body,
                                                               HttpServletRequest request) {
        try {
            String uid      = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            String lessonId = (String) body.get("lessonId");
            int    progress = ((Number) body.getOrDefault("progress", 0)).intValue();
            return ResponseEntity.ok(enrollmentService.updateProgress(uid, courseId, lessonId, progress));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
