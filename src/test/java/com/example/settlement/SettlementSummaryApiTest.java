package com.example.settlement;

import com.example.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettlementSummaryApiTest extends BaseIntegrationTest {

    @Test
    @DisplayName("기간 내 크리에이터별 정산 예정 금액 목록과 전체 합계를 반환한다")
    void 크리에이터별_정산_목록_및_전체_합계() throws Exception {
        // 2025-03: creator-1(120,000) + creator-2(48,000) = 168,000
        // creator-3은 3월 데이터 없어 미포함
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2025-03-01"))
                .andExpect(jsonPath("$.to").value("2025-03-31"))
                .andExpect(jsonPath("$.settlements.length()").value(2))
                .andExpect(jsonPath("$.totalSettlementAmount").value(168000));
    }

    @Test
    @DisplayName("크리에이터별 정산 예정 금액이 정확하게 계산된다")
    void 크리에이터별_정산_예정_금액_정확성() throws Exception {
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlements[0].creatorId").value("creator-1"))
                .andExpect(jsonPath("$.settlements[0].creatorName").value("김강사"))
                .andExpect(jsonPath("$.settlements[0].totalSales").value(260000))
                .andExpect(jsonPath("$.settlements[0].totalRefunds").value(110000))
                .andExpect(jsonPath("$.settlements[0].settlementAmount").value(120000))
                .andExpect(jsonPath("$.settlements[1].creatorId").value("creator-2"))
                .andExpect(jsonPath("$.settlements[1].creatorName").value("이강사"))
                .andExpect(jsonPath("$.settlements[1].totalSales").value(60000))
                .andExpect(jsonPath("$.settlements[1].totalRefunds").value(0))
                .andExpect(jsonPath("$.settlements[1].settlementAmount").value(48000));
    }

    @Test
    @DisplayName("기간 내 데이터가 없는 크리에이터는 목록에서 제외된다")
    void 데이터_없는_크리에이터_제외() throws Exception {
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlements.length()").value(2))
                .andExpect(jsonPath("$.settlements[?(@.creatorId == 'creator-3')]").isEmpty());
    }

    @Test
    @DisplayName("기간을 좁히면 해당 기간의 크리에이터만 포함된다")
    void 기간_필터_적용() throws Exception {
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-02-01")
                        .param("to", "2025-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlements.length()").value(2))
                .andExpect(jsonPath("$.settlements[?(@.creatorId == 'creator-1')]").isEmpty());
    }

    @Test
    @DisplayName("판매는 paidAt, 취소는 canceledAt 기준으로 각각 집계된다")
    void 판매_취소_기준_필드_독립_집계() throws Exception {
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-02-01")
                        .param("to", "2025-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlements[0].creatorId").value("creator-2"))
                .andExpect(jsonPath("$.settlements[0].totalSales").value(0))
                .andExpect(jsonPath("$.settlements[0].totalRefunds").value(60000))
                .andExpect(jsonPath("$.settlements[0].netSales").value(-60000));
    }

    @Test
    @DisplayName("해당 기간에 데이터가 전혀 없으면 빈 목록과 합계 0을 반환한다")
    void 데이터_없는_기간_조회() throws Exception {
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlements.length()").value(0))
                .andExpect(jsonPath("$.totalSettlementAmount").value(0));
    }

    @Test
    @DisplayName("순판매금액이 음수인 creator도 summary 목록에 명시적으로 포함된다")
    void 순판매_음수_creator_summary에_포함() throws Exception {
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-02-01")
                        .param("to", "2025-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlements[?(@.creatorId == 'creator-2')]").isNotEmpty())
                .andExpect(jsonPath("$.settlements[0].creatorId").value("creator-2"))
                .andExpect(jsonPath("$.settlements[0].netSales").value(-60000))
                .andExpect(jsonPath("$.settlements[0].settlementAmount").value(-48000));
    }

    @Test
    @DisplayName("동일 creator의 여러 course 판매가 creator 단위로 합산되어 하나의 항목으로 집계된다")
    void 여러_course_판매가_creator_단위로_합산된다() throws Exception {
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlements.length()").value(2))
                .andExpect(jsonPath("$.settlements[0].creatorId").value("creator-1"))
                .andExpect(jsonPath("$.settlements[0].totalSales").value(260000))
                .andExpect(jsonPath("$.settlements[0].saleCount").value(4))
                .andExpect(jsonPath("$.settlements[0].totalRefunds").value(110000))
                .andExpect(jsonPath("$.settlements[0].cancellationCount").value(2));
    }
}
