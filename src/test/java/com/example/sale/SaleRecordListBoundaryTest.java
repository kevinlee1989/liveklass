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
class SaleRecordListBoundaryTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── 시작일 경계 ────────────────────────────────────────────

    @Test
    @DisplayName("paidAt이 시작일 당일이면 결과에 포함된다")
    void 시작일_당일_paidAt_포함() throws Exception {
        // sale-1: paidAt = 2025-03-05T10:00:00+09:00
        // from = 2025-03-05 → 경계: 2025-03-05T00:00:00+09:00 이상 → 포함
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2025-03-05")
                        .param("to", "2025-03-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("sale-1"));
    }

    @Test
    @DisplayName("paidAt이 시작일 하루 전이면 결과에서 제외된다")
    void 시작일_하루전_paidAt_제외() throws Exception {
        // sale-1: paidAt = 2025-03-05T10:00:00+09:00
        // from = 2025-03-06 → sale-1 제외
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2025-03-06")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'sale-1')]").isEmpty());
    }

    // ── 종료일 경계 ────────────────────────────────────────────

    @Test
    @DisplayName("paidAt이 종료일 당일이면 결과에 포함된다")
    void 종료일_당일_paidAt_포함() throws Exception {
        // sale-4: paidAt = 2025-03-22T11:00:00+09:00
        // to = 2025-03-22 → 경계: 2025-03-23T00:00:00+09:00 미만 → 포함
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2025-03-22")
                        .param("to", "2025-03-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("sale-4"));
    }

    @Test
    @DisplayName("paidAt이 종료일 하루 뒤이면 결과에서 제외된다")
    void 종료일_하루뒤_paidAt_제외() throws Exception {
        // sale-4: paidAt = 2025-03-22T11:00:00+09:00
        // to = 2025-03-21 → sale-4 제외
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'sale-4')]").isEmpty());
    }

    // ── 파라미터 유효성 ────────────────────────────────────────

    @Test
    @DisplayName("from이 to보다 늦으면 400을 반환한다")
    void from이_to보다_늦으면_400() throws Exception {
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2025-03-31")
                        .param("to", "2025-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("시작일(from)은 종료일(to)보다 늦을 수 없습니다."));
    }

    @Test
    @DisplayName("from만 입력하면 400을 반환한다")
    void from만_입력시_400() throws Exception {
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("from", "2025-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("from과 to는 함께 입력하거나 함께 생략해야 합니다."));
    }

    @Test
    @DisplayName("to만 입력하면 400을 반환한다")
    void to만_입력시_400() throws Exception {
        mockMvc.perform(get("/sale-records")
                        .param("creatorId", "creator-1")
                        .param("to", "2025-03-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("from과 to는 함께 입력하거나 함께 생략해야 합니다."));
    }
}
