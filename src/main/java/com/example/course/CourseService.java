package com.example.course;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.course.dto.CourseRequest;
import com.example.course.dto.CourseResponse;
import com.example.course.dto.CourseTitleUpdateRequest;
import com.example.creator.CreatorMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final CreatorMapper creatorMapper;

    @Transactional(readOnly = true)
    public List<CourseResponse> getList(String creatorId) {
        List<Course> courses = creatorId != null
                ? courseMapper.findByCreatorId(creatorId)
                : courseMapper.findAll();
        return courses.stream().map(CourseResponse::from).toList();
    }

    @Transactional
    public String register(CourseRequest request) {
        if (courseMapper.existsById(request.id())) {
            throw new IllegalArgumentException("이미 존재하는 강의 ID입니다: " + request.id());
        }

        if (!creatorMapper.existsById(request.creatorId())) {
            throw new IllegalArgumentException("존재하지 않는 크리에이터입니다: " + request.creatorId());
        }

        Course course = Course.of(request.id(), request.creatorId(), request.title());
        courseMapper.insert(course);
        return course.getId();
    }

    @Transactional
    public CourseResponse updateTitle(String courseId, CourseTitleUpdateRequest request) {
        Course course = courseMapper.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지않는 강의이다 " + courseId));

        course.update(request.title());
        courseMapper.update(course);

        return CourseResponse.from(course);
    }

    @Transactional
    public void delete(String courseId) {
        if (!courseMapper.existsById(courseId)) {
            throw new IllegalArgumentException("존재하지않는 강의이다 " + courseId);
        }

        courseMapper.deleteById(courseId);
    }
}
