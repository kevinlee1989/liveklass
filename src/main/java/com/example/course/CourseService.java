package com.example.course;

import com.example.course.dto.CourseRequest;
import com.example.creator.Creator;
import com.example.creator.CreatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CreatorRepository creatorRepository;

    @Transactional
    public String register(CourseRequest request) {
        if (courseRepository.existsById(request.id())) {
            throw new IllegalArgumentException("이미 존재하는 강의 ID입니다: " + request.id());
        }

        Creator creator = creatorRepository.findById(request.creatorId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 크리에이터입니다: " + request.creatorId()));

        Course course = Course.of(request.id(), creator, request.title());
        return courseRepository.save(course).getId();
    }
}
