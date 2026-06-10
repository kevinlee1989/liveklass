package com.example.settlement;

import com.example.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettlementEdgeCaseTest extends BaseIntegrationTest {

    // ── 순판매금액 = 0 ─────────────────────────────────────────────

    @Test
    @DisplayName("판매금액과 환불금액이 동일하면 수수료는 0이고 정산금액도 0이다")
    void 순판매금액이_0이면_수수료와_정산금액도_0() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "edge-sale-zero",
                                  "courseId": "course-4",
                                  "studentId": "student-edge",
                                  "amount": 50000,
                                  "paidAt": "2025-04-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "edge-sale-zero",
                                  "refundAmount": 50000,
                                  "canceledAt": "2025-04-15T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/settlements/creators/creator-3")
                        .param("month", "2025-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(50000))
                .andExpect(jsonPath("$.totalRefunds").value(50000))
                .andExpect(jsonPath("$.netSales").value(0))
                .andExpect(jsonPath("$.platformFee").value(0))
                .andExpect(jsonPath("$.settlementAmount").value(0))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancellationCount").value(1));
    }

    // ── 소수점 버림 ────────────────────────────────────────────────

    @Test
    @DisplayName("수수료 계산은 소수점 이하를 버림으로 처리한다 — 999 × 20% = 199.8 → 199원")
    void 수수료_소수점_버림_999원() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "edge-sale-999",
                                  "courseId": "course-4",
                                  "studentId": "student-edge",
                                  "amount": 999,
                                  "paidAt": "2025-04-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/settlements/creators/creator-3")
                        .param("month", "2025-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netSales").value(999))
                .andExpect(jsonPath("$.platformFee").value(199))
                .andExpect(jsonPath("$.settlementAmount").value(800));
    }

    @Test
    @DisplayName("수수료 계산은 소수점 이하를 버림으로 처리한다 — 1001 × 20% = 200.2 → 200원")
    void 수수료_소수점_버림_1001원() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "edge-sale-1001",
                                  "courseId": "course-4",
                                  "studentId": "student-edge",
                                  "amount": 1001,
                                  "paidAt": "2025-04-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/settlements/creators/creator-3")
                        .param("month", "2025-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netSales").value(1001))
                .andExpect(jsonPath("$.platformFee").value(200))
                .andExpect(jsonPath("$.settlementAmount").value(801));
    }

    // ── 음수 netSales의 수수료 처리 ────────────────────────────────

    @Test
    @DisplayName("순판매금액이 음수이면 수수료도 음수로 계산되어 정산금액에서 차감이 환원된다")
    void 순판매금액_음수일때_수수료_확인() throws Exception {
        mockMvc.perform(get("/settlements/creators/creator-2")
                        .param("month", "2025-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(0))
                .andExpect(jsonPath("$.totalRefunds").value(60000))
                .andExpect(jsonPath("$.netSales").value(-60000))
                .andExpect(jsonPath("$.platformFee").value(-12000))
                .andExpect(jsonPath("$.settlementAmount").value(-48000));
    }

    // ── summary: netSales = 0인 크리에이터 포함 여부 ───────────────

    @Test
    @DisplayName("판매와 환불이 모두 있어 netSales=0인 크리에이터는 summary 목록에 포함된다")
    void 순판매금액_0인_크리에이터_summary에_포함() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "edge-summary-zero",
                                  "courseId": "course-4",
                                  "studentId": "student-edge",
                                  "amount": 50000,
                                  "paidAt": "2025-04-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "edge-summary-zero",
                                  "refundAmount": 50000,
                                  "canceledAt": "2025-04-15T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-04-01")
                        .param("to", "2025-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlements.length()").value(1))
                .andExpect(jsonPath("$.settlements[0].creatorId").value("creator-3"))
                .andExpect(jsonPath("$.settlements[0].netSales").value(0))
                .andExpect(jsonPath("$.settlements[0].settlementAmount").value(0))
                .andExpect(jsonPath("$.totalSettlementAmount").value(0));
    }

    // ── 여러 건 판매 + 여러 건 부분환불 합산 정확성 ─────────────────

    @Test
    @DisplayName("여러 건 판매와 여러 건 부분 환불의 합산이 정확하게 계산된다")
    void 여러건_판매_여러건_부분환불_합산_정확성() throws Exception {
        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "edge-multi-1",
                                  "courseId": "course-4",
                                  "studentId": "student-a",
                                  "amount": 10000,
                                  "paidAt": "2025-04-01T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "edge-multi-2",
                                  "courseId": "course-4",
                                  "studentId": "student-b",
                                  "amount": 20000,
                                  "paidAt": "2025-04-05T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/sale-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "edge-multi-3",
                                  "courseId": "course-4",
                                  "studentId": "student-c",
                                  "amount": 30000,
                                  "paidAt": "2025-04-10T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "edge-multi-1",
                                  "refundAmount": 5000,
                                  "canceledAt": "2025-04-20T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/cancellation-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleRecordId": "edge-multi-2",
                                  "refundAmount": 7000,
                                  "canceledAt": "2025-04-25T10:00:00+09:00"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/settlements/creators/creator-3")
                        .param("month", "2025-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(60000))
                .andExpect(jsonPath("$.totalRefunds").value(12000))
                .andExpect(jsonPath("$.netSales").value(48000))
                .andExpect(jsonPath("$.platformFee").value(9600))
                .andExpect(jsonPath("$.settlementAmount").value(38400))
                .andExpect(jsonPath("$.saleCount").value(3))
                .andExpect(jsonPath("$.cancellationCount").value(2));
    }
}
