package com.example.sale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class SaleRecordListApiTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("기간 필터 없이 조회하면 크리에이터의 전체 판매 내역을 반환한다")
    void 전체_판매_내역_조회() throws Exception {
        // creator-1 의 판매: sale-1,2,3,4 총 4건
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DisplayName("기간 필터를 적용하면 해당 기간의 판매 내역만 반환한다")
    void 기간_필터_판매_내역_조회() throws Exception {
        // creator-1, 2025-03 판매: sale-1,2,3,4 총 4건
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DisplayName("기간 필터를 적용하면 해당 기간 외 판매 내역은 제외된다")
    void 기간_필터_범위_밖_제외() throws Exception {
        // creator-2, 2025-03 판매: sale-6 만 1건 (sale-5는 1월 판매라 제외)
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-2")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("sale-6"));
    }

    @Test
    @DisplayName("응답에 courseId, courseTitle, creatorId 가 포함된다")
    void 응답_필드_확인() throws Exception {
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseId").exists())
                .andExpect(jsonPath("$[0].courseTitle").exists())
                .andExpect(jsonPath("$[0].creatorId").value("creator-1"))
                .andExpect(jsonPath("$[0].studentId").exists())
                .andExpect(jsonPath("$[0].amount").exists())
                .andExpect(jsonPath("$[0].paidAt").exists());
    }

    @Test
    @DisplayName("판매 내역이 없는 크리에이터 조회 시 빈 배열을 반환한다")
    void 판매_내역_없는_크리에이터_조회() throws Exception {
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
