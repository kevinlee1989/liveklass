package com.example.course.dto;

import com.example.course.Course;

public record CourseResponse(
        String id,
        String creatorId,
        String title
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCreator().getId(),
                course.getTitle()
        );
    }
}
