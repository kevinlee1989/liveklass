package com.example.course;

import com.example.creator.Creator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Column(name = "title", nullable = false)
    private String title;

    public static Course of(String id, Creator creator, String title) {
        Course course = new Course();
        course.id = id;
        course.creator = creator;
        course.title = title;
        return course;
    }
}
