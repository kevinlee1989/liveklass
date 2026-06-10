package com.example.course;

import com.example.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseApiTest extends BaseIntegrationTest {

    // ── 제목 수정 ────────────────────────────────────────────────────

    @Test
    @DisplayName("강의 제목 수정 성공 시 변경된 제목과 200을 반환한다")
    void 강의_제목_수정_성공() throws Exception {
        mockMvc.perform(patch("/courses/course-1/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Spring Boot 심화" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("course-1"))
                .andExpect(jsonPath("$.creatorId").value("creator-1"))
                .andExpect(jsonPath("$.title").value("Spring Boot 심화"));
    }

    @Test
    @DisplayName("존재하지 않는 강의 제목 수정 시 400을 반환한다")
    void 존재하지않는_강의_제목_수정시_400() throws Exception {
        mockMvc.perform(patch("/courses/course-999/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "새 제목" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("존재하지않는 강의이다 course-999"));
    }

    @Test
    @DisplayName("빈 문자열로 제목 수정 요청 시 400을 반환한다")
    void 빈_제목으로_수정_요청시_400() throws Exception {
        mockMvc.perform(patch("/courses/course-1/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("공백만 있는 제목으로 수정 요청 시 400을 반환한다")
    void 공백_제목으로_수정_요청시_400() throws Exception {
        mockMvc.perform(patch("/courses/course-1/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "   " }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── 강의 삭제 ────────────────────────────────────────────────────

    @Test
    @DisplayName("강의 삭제 성공 시 204를 반환한다")
    void 강의_삭제_성공() throws Exception {
        // 기존 강의(course-1~4)는 SaleRecord FK가 있어 삭제 불가 → 새 강의 등록 후 삭제
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "id": "course-del", "creatorId": "creator-1", "title": "삭제용 강의" }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/courses/course-del"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 강의 삭제 시 400을 반환한다")
    void 존재하지않는_강의_삭제시_400() throws Exception {
        mockMvc.perform(delete("/courses/course-999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("존재하지않는 강의이다 course-999"));
    }
}
