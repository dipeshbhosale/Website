package com.skillnest.course.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    private String id;
    private String title;
    private String description;
    private String instructor;
    private String instructorId;
    private String category;
    private String level;           // beginner | intermediate | advanced
    private double price;
    private double rating;
    private int totalStudents;
    private int totalLessons;
    private String duration;        // e.g. "12h 30m"
    private String thumbnail;
    private boolean isFeatured;
    private boolean isFree;
    private List<String> tags;
    private List<Map<String, Object>> curriculum;
    private String createdAt;
    private String updatedAt;
}
