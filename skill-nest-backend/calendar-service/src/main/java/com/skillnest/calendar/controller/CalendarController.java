package com.skillnest.calendar.controller;

import com.skillnest.calendar.service.CalendarService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    // GET /api/calendar — all events for current user
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getEvents(HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(calendarService.getUserEvents(uid));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/calendar/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEvent(@PathVariable String id,
                                                         HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            Map<String, Object> event = calendarService.getEvent(id, uid);
            return event != null ? ResponseEntity.ok(event) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST /api/calendar
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(@RequestBody Map<String, Object> body,
                                                            HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(calendarService.createEvent(body, uid));
        } catch (Exception e) {
            log.error("Create event failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // PUT /api/calendar/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEvent(@PathVariable String id,
                                                            @RequestBody Map<String, Object> body,
                                                            HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            Map<String, Object> updated = calendarService.updateEvent(id, body, uid);
            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DELETE /api/calendar/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable String id,
                                                            HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            if (uid == null) return ResponseEntity.status(401).build();
            boolean deleted = calendarService.deleteEvent(id, uid);
            return deleted
                    ? ResponseEntity.ok(Map.of("message", "Event deleted"))
                    : ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
