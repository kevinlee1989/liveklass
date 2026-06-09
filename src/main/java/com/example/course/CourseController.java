package com.example.course;

import com.example.course.dto.CourseRequest;
import com.example.course.dto.CourseTitleUpdateRequest;
import com.example.course.dto.CourseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody CourseRequest request) {
        String id = courseService.register(request);
        return Map.of("id", id);
    }

    @PatchMapping("/{courseId}/title")
    public CourseResponse updateTitle(
        @PathVariable String courseId,
        @Valid @RequestBody CourseTitleUpdateRequest request
    ) {
        return courseService.updateTitle(courseId, request);
    }

    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable String courseId
    ) {
        courseService.delete(courseId);
    }
}
