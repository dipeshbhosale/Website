package com.skillnest.course.controller;

import com.skillnest.course.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // GET /api/courses?category=&level=&free=false
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "false") boolean free) {
        try {
            return ResponseEntity.ok(courseService.getAllCourses(category, level, free));
        } catch (Exception e) {
            log.error("Failed to fetch courses: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/courses/featured
    @GetMapping("/featured")
    public ResponseEntity<List<Map<String, Object>>> getFeatured() {
        try {
            return ResponseEntity.ok(courseService.getFeaturedCourses());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/courses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourse(@PathVariable String id) {
        try {
            Map<String, Object> course = courseService.getCourseById(id);
            return course != null ? ResponseEntity.ok(course) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST /api/courses  [teacher only]
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourse(@RequestBody Map<String, Object> body,
                                                             HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(courseService.createCourse(body, uid));
        } catch (Exception e) {
            log.error("Create course failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // PUT /api/courses/{id}  [teacher who owns the course]
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCourse(@PathVariable String id,
                                                             @RequestBody Map<String, Object> body,
                                                             HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            Map<String, Object> updated = courseService.updateCourse(id, body, uid);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DELETE /api/courses/{id}  [teacher who owns the course]
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCourse(@PathVariable String id,
                                                             HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            boolean deleted = courseService.deleteCourse(id, uid);
            return deleted
                    ? ResponseEntity.ok(Map.of("message", "Course deleted"))
                    : ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST /api/courses/{id}/rate
    @PostMapping("/{id}/rate")
    public ResponseEntity<Map<String, Object>> rateCourse(@PathVariable String id,
                                                           @RequestBody Map<String, Object> body,
                                                           HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            double rating = ((Number) body.get("rating")).doubleValue();
            return ResponseEntity.ok(courseService.rateCourse(id, uid, rating));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
