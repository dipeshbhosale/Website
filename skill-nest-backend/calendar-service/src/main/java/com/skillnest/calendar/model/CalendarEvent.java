package com.skillnest.calendar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {
    private String id;
    private String title;
    private String type;       // class | assignment | webinar | exam | officehours
    private String start;      // ISO-8601
    private String end;        // ISO-8601
    private String location;
    private String notes;
    private String userId;
    private String courseId;   // optional — link event to a course
    private boolean allDay;
    private String createdAt;
}
