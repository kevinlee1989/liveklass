package com.example.course;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Course {

    private String id;
    private String creatorId;
    private String title;

    public static Course of(String id, String creatorId, String title) {
        Course course = new Course();
        course.id = id;
        course.creatorId = creatorId;
        course.title = title;
        return course;
    }

    public void update(String title) {
        this.title = title;
    }
}
