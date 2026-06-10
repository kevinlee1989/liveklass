package com.example.course;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper {
    boolean existsById(String id);
    void insert(Course course);
    Optional<Course> findById(String id);
    void update(Course course);
    void deleteById(String id);
}
