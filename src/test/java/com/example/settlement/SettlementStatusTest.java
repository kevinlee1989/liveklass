package com.example.settlement;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class SettlementStatusTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── PENDING (기본 상태) ────────────────────────────────────────

    @Test
    @DisplayName("확정 전 GET 조회는 status=PENDING으로 동적 계산 결과를 반환한다")
    void 확정전_GET_조회는_PENDING_반환() throws Exception {
        mockMvc.perform(get("/settlements/creators/creator-1")
                        .param("month", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.confirmedAt").doesNotExist())
                .andExpect(jsonPath("$.paidAt").doesNotExist());
    }

    // ── CONFIRMED 정상 전환 ───────────────────────────────────────

    @Test
    @DisplayName("CONFIRMED 요청 시 정산이 계산되고 스냅샷이 저장된다")
    void CONFIRMED_요청시_스냅샷_저장() throws Exception {
        // creator-1, 2025-03: totalSales=260,000 / totalRefunds=110,000 / netSales=150,000
        // platformFee=30,000 / settlementAmount=120,000
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalSales").value(260000))
                .andExpect(jsonPath("$.totalRefunds").value(110000))
                .andExpect(jsonPath("$.netSales").value(150000))
                .andExpect(jsonPath("$.platformFee").value(30000))
                .andExpect(jsonPath("$.settlementAmount").value(120000))
                .andExpect(jsonPath("$.confirmedAt").exists())
                .andExpect(jsonPath("$.paidAt").doesNotExist());
    }

    @Test
    @DisplayName("CONFIRMED 후 GET 조회는 스냅샷을 반환한다 (동적 계산 아님)")
    void CONFIRMED_후_GET_조회는_스냅샷_반환() throws Exception {
        // 확정 처리
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isOk());

        // GET 조회 → 스냅샷 반환
        mockMvc.perform(get("/settlements/creators/creator-1")
                        .param("month", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.settlementAmount").value(120000))
                .andExpect(jsonPath("$.confirmedAt").exists());
    }

    // ── PAID 정상 전환 ────────────────────────────────────────────

    @Test
    @DisplayName("CONFIRMED → PAID 전환 시 paidAt이 기록된다")
    void CONFIRMED_후_PAID_전환_성공() throws Exception {
        // CONFIRMED 먼저
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isOk());

        // PAID 전환
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.confirmedAt").exists())
                .andExpect(jsonPath("$.paidAt").exists());
    }

    @Test
    @DisplayName("PAID 후 GET 조회는 PAID 상태와 paidAt을 반환한다")
    void PAID_후_GET_조회() throws Exception {
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID" }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/settlements/creators/creator-1")
                        .param("month", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAt").exists());
    }

    // ── 중복 정산 방지 ────────────────────────────────────────────

    @Test
    @DisplayName("이미 CONFIRMED된 정산에 CONFIRMED 재요청 시 400을 반환한다")
    void CONFIRMED_중복_요청시_400() throws Exception {
        // 1차 확정
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isOk());

        // 2차 확정 시도
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("이미 확정된 정산입니다")));
    }

    @Test
    @DisplayName("PAID 상태에서 CONFIRMED 재요청 시 400을 반환한다")
    void PAID_후_CONFIRMED_재요청시_400() throws Exception {
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID" }
                                """))
                .andExpect(status().isOk());

        // CONFIRMED 재시도
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("이미 지급 완료된 정산입니다")));
    }

    // ── 잘못된 전이 순서 ──────────────────────────────────────────

    @Test
    @DisplayName("CONFIRMED 없이 PAID 요청 시 400을 반환한다")
    void CONFIRMED_없이_PAID_요청시_400() throws Exception {
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("확정되지 않은 정산입니다")));
    }

    @Test
    @DisplayName("PAID 후 PAID 재요청 시 400을 반환한다 (재지급 방지)")
    void PAID_후_PAID_재요청시_400() throws Exception {
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CONFIRMED" }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID" }
                                """))
                .andExpect(status().isOk());

        // PAID 재시도
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PAID" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("이미 지급 완료된 정산입니다")));
    }

    @Test
    @DisplayName("PENDING으로 직접 전환 요청 시 400을 반환한다")
    void PENDING_직접_전환_요청시_400() throws Exception {
        mockMvc.perform(patch("/settlements/creators/creator-1")
                        .param("month", "2025-03")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "PENDING" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("PENDING으로 직접 전환할 수 없습니다."));
    }
}
