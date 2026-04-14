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
class SaleRecordApiTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("판매 내역을 정상적으로 등록하면 201과 id를 반환한다")
    void 판매_내역_정상_등록() throws Exception {
        String request = """
                {
                  "id": "test-sale-1",
                  "courseId": "course-1",
                  "studentId": "student-99",
                  "amount": 50000,
                  "paidAt": "2025-04-01T10:00:00+09:00"
                }
                """;

        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("test-sale-1"));
    }

    @Test
    @DisplayName("존재하지 않는 courseId로 등록하면 400을 반환한다")
    void 존재하지_않는_courseId_등록시_400() throws Exception {
        String request = """
                {
                  "id": "test-sale-err",
                  "courseId": "course-999",
                  "studentId": "student-99",
                  "amount": 50000,
                  "paidAt": "2025-04-01T10:00:00+09:00"
                }
                """;

        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("존재하지 않는 강의입니다: course-999"));
    }

    @Test
    @DisplayName("이미 존재하는 id로 등록하면 400을 반환한다")
    void 중복_id_등록시_400() throws Exception {
        String request = """
                {
                  "id": "sale-1",
                  "courseId": "course-1",
                  "studentId": "student-99",
                  "amount": 50000,
                  "paidAt": "2025-04-01T10:00:00+09:00"
                }
                """;

        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("이미 존재하는 판매 내역 ID입니다: sale-1"));
    }
}
