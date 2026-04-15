package com.example.settlement;

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
class SettlementCalculationApiTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("[과제 시나리오 1] creator-1의 2025-03 정산 금액이 정확하게 계산된다")
    void creator1_2025년3월_정산_계산() throws Exception {
        // sale-1(50,000) + sale-2(50,000) + sale-3(80,000) + sale-4(80,000) = 260,000
        // cancel-1(80,000 전액환불) + cancel-2(30,000 부분환불) = 110,000
        // netSales = 150,000 / platformFee = 30,000 / settlementAmount = 120,000
        mockMvc.perform(get("/settlements/creators/creator-1")
                        .param("month", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creatorId").value("creator-1"))
                .andExpect(jsonPath("$.month").value("2025-03"))
                .andExpect(jsonPath("$.totalSales").value(260000))
                .andExpect(jsonPath("$.totalRefunds").value(110000))
                .andExpect(jsonPath("$.netSales").value(150000))
                .andExpect(jsonPath("$.platformFee").value(30000))
                .andExpect(jsonPath("$.settlementAmount").value(120000))
                .andExpect(jsonPath("$.saleCount").value(4))
                .andExpect(jsonPath("$.cancellationCount").value(2));
    }

    @Test
    @DisplayName("[과제 시나리오 2] 부분 환불은 환불액만큼만 차감된다")
    void 부분_환불_차감_확인() throws Exception {
        // sale-4(80,000)에 cancel-2(30,000 부분환불) → 50,000이 순판매에 기여
        // 시나리오 1 결과에서 totalRefunds = 80,000 + 30,000 = 110,000 으로 확인
        mockMvc.perform(get("/settlements/creators/creator-1")
                        .param("month", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRefunds").value(110000)); // 80,000 전액 + 30,000 부분
    }

    @Test
    @DisplayName("[과제 시나리오 3] 월 경계 — 1월 말 판매는 1월 정산에 포함된다")
    void 월경계_1월말_판매는_1월_정산에_포함() throws Exception {
        // sale-5: paidAt = 2025-01-31T23:30:00+09:00 → 1월 판매
        // cancel-3: canceledAt = 2025-02-03 → 2월 취소 (1월 정산에는 미포함)
        mockMvc.perform(get("/settlements/creators/creator-2")
                        .param("month", "2025-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(60000))
                .andExpect(jsonPath("$.totalRefunds").value(0))
                .andExpect(jsonPath("$.netSales").value(60000))
                .andExpect(jsonPath("$.platformFee").value(12000))
                .andExpect(jsonPath("$.settlementAmount").value(48000))
                .andExpect(jsonPath("$.saleCount").value(1))
                .andExpect(jsonPath("$.cancellationCount").value(0));
    }

    @Test
    @DisplayName("[과제 시나리오 3] 월 경계 — 2월 초 취소는 2월 정산에 반영된다")
    void 월경계_2월초_취소는_2월_정산에_반영() throws Exception {
        // cancel-3: canceledAt = 2025-02-03 → 2월 취소로 반영
        // sale-6(2025-03-10)는 3월 판매이므로 2월 판매는 0
        mockMvc.perform(get("/settlements/creators/creator-2")
                        .param("month", "2025-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(0))
                .andExpect(jsonPath("$.totalRefunds").value(60000))
                .andExpect(jsonPath("$.netSales").value(-60000))
                .andExpect(jsonPath("$.saleCount").value(0))
                .andExpect(jsonPath("$.cancellationCount").value(1));
    }

    @Test
    @DisplayName("[과제 시나리오 4] 판매 내역이 없는 월 조회 시 모든 금액이 0이다")
    void 빈_월_조회시_모두_0() throws Exception {
        // creator-3은 2025-02에 sale-7(120,000) 판매, 2025-03에는 데이터 없음
        mockMvc.perform(get("/settlements/creators/creator-3")
                        .param("month", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(0))
                .andExpect(jsonPath("$.totalRefunds").value(0))
                .andExpect(jsonPath("$.netSales").value(0))
                .andExpect(jsonPath("$.platformFee").value(0))
                .andExpect(jsonPath("$.settlementAmount").value(0))
                .andExpect(jsonPath("$.saleCount").value(0))
                .andExpect(jsonPath("$.cancellationCount").value(0));
    }

    @Test
    @DisplayName("수수료는 순판매액의 20%를 원 단위 버림으로 계산한다")
    void 수수료_계산_정확성() throws Exception {
        // creator-2, 2025-03: sale-6 = 60,000
        // netSales = 60,000 / platformFee = 60,000 * 0.2 = 12,000 / settlementAmount = 48,000
        mockMvc.perform(get("/settlements/creators/creator-2")
                        .param("month", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSales").value(60000))
                .andExpect(jsonPath("$.totalRefunds").value(0))
                .andExpect(jsonPath("$.netSales").value(60000))
                .andExpect(jsonPath("$.platformFee").value(12000))
                .andExpect(jsonPath("$.settlementAmount").value(48000));
    }
}
