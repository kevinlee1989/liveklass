package com.example.sale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class SaleRecordValidationTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── amount ──────────────────────────────────────────────

    @Test
    @DisplayName("amount가 0이면 400을 반환한다 — 0원 판매는 허용하지 않는다")
    void amount_0이면_400() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "test-sale",
                                  "courseId": "course-1",
                                  "studentId": "student-1",
                                  "amount": 0,
                                  "paidAt": "2025-03-01T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("amount가 음수이면 400을 반환한다")
    void amount_음수이면_400() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "test-sale",
                                  "courseId": "course-1",
                                  "studentId": "student-1",
                                  "amount": -1000,
                                  "paidAt": "2025-03-01T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── studentId ────────────────────────────────────────────

    @Test
    @DisplayName("studentId가 빈 문자열이면 400을 반환한다")
    void studentId_빈문자열이면_400() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "test-sale",
                                  "courseId": "course-1",
                                  "studentId": "",
                                  "amount": 50000,
                                  "paidAt": "2025-03-01T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("studentId가 null이면 400을 반환한다")
    void studentId_null이면_400() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "test-sale",
                                  "courseId": "course-1",
                                  "studentId": null,
                                  "amount": 50000,
                                  "paidAt": "2025-03-01T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── courseId ─────────────────────────────────────────────

    @Test
    @DisplayName("courseId가 빈 문자열이면 400을 반환한다")
    void courseId_빈문자열이면_400() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "test-sale",
                                  "courseId": "",
                                  "studentId": "student-1",
                                  "amount": 50000,
                                  "paidAt": "2025-03-01T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("courseId가 null이면 400을 반환한다")
    void courseId_null이면_400() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "test-sale",
                                  "courseId": null,
                                  "studentId": "student-1",
                                  "amount": 50000,
                                  "paidAt": "2025-03-01T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── paidAt ───────────────────────────────────────────────

    @Test
    @DisplayName("paidAt이 null이면 400을 반환한다")
    void paidAt_null이면_400() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "test-sale",
                                  "courseId": "course-1",
                                  "studentId": "student-1",
                                  "amount": 50000,
                                  "paidAt": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("paidAt 형식이 잘못되면 400을 반환한다")
    void paidAt_잘못된_형식이면_400() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "test-sale",
                                  "courseId": "course-1",
                                  "studentId": "student-1",
                                  "amount": 50000,
                                  "paidAt": "2025-03-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
