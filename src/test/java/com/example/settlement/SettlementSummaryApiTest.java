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
class SettlementSummaryApiTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

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
        // creator-1: netSales=150,000, fee=30,000, settlement=120,000
        // creator-2: netSales=60,000,  fee=12,000, settlement=48,000
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
        // 2025-03: creator-3은 sale-7이 2월 판매라 3월 목록에서 제외
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
        // 2025-02: creator-2(cancel-3), creator-3(sale-7) 만 포함
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
        // 2025-02: sale-5는 1월 판매라 미포함, cancel-3은 2월 취소라 포함
        // 결과: creator-2(index=0), creator-3(index=1) — creatorId 정렬
        // creator-2: totalSales=0, totalRefunds=60,000 (cancel-3만 해당)
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

    // ── 집계 대상 포함 기준 ─────────────────────────────────────────

    @Test
    @DisplayName("순판매금액이 음수인 creator도 summary 목록에 명시적으로 포함된다")
    void 순판매_음수_creator_summary에_포함() throws Exception {
        // creator-2, 2025-02: 판매 0건(sale-5는 1월 판매), 취소 1건(cancel-3: 60,000)
        // netSales = -60,000 → 음수임에도 목록에 포함되어야 한다
        // fee = -60,000 * 0.20 = -12,000 → settlementAmount = -48,000
        //
        // [검증 방법]
        // 1. [?(@.creatorId == 'creator-2')] 필터로 creator-2가 목록에 존재하는지 확인
        // 2. 인덱스 직접 접근으로 금액이 음수로 집계되었는지 확인
        //    (2025-02는 creator-2, creator-3 두 명 → creatorId 정렬 → index 0 = creator-2)
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-02-01")
                        .param("to", "2025-02-28"))
                .andExpect(status().isOk())
                // 음수 creator가 목록에 존재한다
                .andExpect(jsonPath("$.settlements[?(@.creatorId == 'creator-2')]").isNotEmpty())
                .andExpect(jsonPath("$.settlements[0].creatorId").value("creator-2"))
                // 해당 creator의 금액이 음수로 올바르게 집계된다
                .andExpect(jsonPath("$.settlements[0].netSales").value(-60000))
                .andExpect(jsonPath("$.settlements[0].settlementAmount").value(-48000));
    }

    @Test
    @DisplayName("동일 creator의 여러 course 판매가 creator 단위로 합산되어 하나의 항목으로 집계된다")
    void 여러_course_판매가_creator_단위로_합산된다() throws Exception {
        // creator-1: course-1에서 sale-1(50,000) + sale-2(50,000)
        //            course-2에서 sale-3(80,000) + sale-4(80,000)
        // → course별로 분리되면 4개 항목이 되어야 하지만, creator 단위로 합산 → 1개 항목
        //
        // [검증 방법]
        // 1. settlements.length() == 2 → course별 분리가 일어나지 않았음을 확인
        //    (course별로 쪼개지면 4개가 되어야 하므로 2개면 creator 단위 집계가 맞음)
        // 2. creator-1의 totalSales = 260,000 (4건 합산), saleCount = 4 확인
        // 3. creator-1의 totalRefunds = 110,000 (cancel-1: 80,000 + cancel-2: 30,000) 확인
        mockMvc.perform(get("/settlements/summary")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                // creator-1이 course별로 분리되지 않고 하나의 항목으로 존재한다
                .andExpect(jsonPath("$.settlements.length()").value(2))
                .andExpect(jsonPath("$.settlements[0].creatorId").value("creator-1"))
                // 두 course(course-1, course-2)의 판매 4건이 하나로 합산된다
                .andExpect(jsonPath("$.settlements[0].totalSales").value(260000))
                .andExpect(jsonPath("$.settlements[0].saleCount").value(4))
                // 두 course에 걸친 취소 2건(cancel-1, cancel-2)도 합산된다
                .andExpect(jsonPath("$.settlements[0].totalRefunds").value(110000))
                .andExpect(jsonPath("$.settlements[0].cancellationCount").value(2));
    }
}
